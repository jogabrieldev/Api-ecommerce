package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.application.usecase.CheckoutCartUseCase;
import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ConflictException;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.CustomerAddress;
import com.api.e_commerce.domain.model.PaymentMethod;
import com.api.e_commerce.domain.model.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class CheckoutConcurrencyIntegrationTest {

    private static final String TOKEN = "SIM-APPROVED-123456789012";

    @Autowired
    private CheckoutCartUseCase checkoutCartUseCase;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearBusinessData() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE payment_allocations, payments, order_items, orders,
                    cart_items, carts, products, customer_addresses, user_identities,
                    customers, categories, administrators CASCADE
                """);
    }

    @Test
    void shouldChargeOnlyOneCustomerForTheLastStockUnit() throws Exception {
        Fixture fixture = createFixture(1, 2);

        List<Outcome> outcomes = runConcurrently(
                checkout(fixture.customers().get(0), "00000000-0000-4000-8000-000000000101"),
                checkout(fixture.customers().get(1), "00000000-0000-4000-8000-000000000102"));

        assertEquals(1, successes(outcomes).size());
        assertEquals(1, failures(outcomes).size());
        assertInstanceOf(BusinessRuleException.class, failures(outcomes).getFirst().failure());
        assertEquals(0, productStock(fixture.productId()));
        assertEquals(1L, count("orders"));
        assertEquals(1L, count("payments"));
    }

    @Test
    void shouldReplayConcurrentCheckoutFromTheSameCustomer() throws Exception {
        Fixture fixture = createFixture(2, 1);
        CustomerData customer = fixture.customers().getFirst();
        String key = "00000000-0000-4000-8000-000000000201";

        List<Outcome> outcomes = runConcurrently(
                checkout(customer, key),
                checkout(customer, key));

        List<Outcome> successful = successes(outcomes);
        assertEquals(2, successful.size());
        assertEquals(1, successful.stream().filter(outcome -> outcome.result().replayed()).count());
        assertEquals(1, productStock(fixture.productId()));
        assertEquals(1L, count("orders"));
        assertEquals(1L, count("payments"));
    }

    @Test
    void shouldAllowOnlyOneCustomerToClaimTheSameIdempotencyKey() throws Exception {
        Fixture fixture = createFixture(2, 2);
        String key = "00000000-0000-4000-8000-000000000301";

        List<Outcome> outcomes = runConcurrently(
                checkout(fixture.customers().get(0), key),
                checkout(fixture.customers().get(1), key));

        assertEquals(1, successes(outcomes).size());
        assertEquals(1, failures(outcomes).size());
        Throwable failure = failures(outcomes).getFirst().failure();
        assertTrue(failure instanceof ConflictException
                        || failure instanceof DataIntegrityViolationException,
                () -> "Unexpected failure: " + failure);
        assertEquals(1, productStock(fixture.productId()));
        assertEquals(1L, count("orders"));
        assertEquals(1L, count("payments"));
    }

    private Callable<CheckoutCartUseCase.Result> checkout(CustomerData customer, String key) {
        return () -> checkoutCartUseCase.execute(
                customer.id(), customer.email(), PaymentMethod.PIX, TOKEN, key);
    }

    @SafeVarargs
    private List<Outcome> runConcurrently(
            Callable<CheckoutCartUseCase.Result>... operations) throws Exception {
        var ready = new CountDownLatch(operations.length);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(operations.length);
        try {
            List<Future<Outcome>> futures = new ArrayList<>();
            for (Callable<CheckoutCartUseCase.Result> operation : operations) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await(5, TimeUnit.SECONDS);
                    try {
                        return new Outcome(operation.call(), null);
                    } catch (Throwable failure) {
                        return new Outcome(null, failure);
                    }
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            List<Outcome> outcomes = new ArrayList<>();
            for (Future<Outcome> future : futures) {
                outcomes.add(future.get(15, TimeUnit.SECONDS));
            }
            return outcomes;
        } finally {
            executor.shutdownNow();
        }
    }

    private Fixture createFixture(int stock, int customerCount) {
        return transactionTemplate.execute(status -> {
            Administrator administrator = new Administrator(
                    "Seller", "seller@email.com", "hash",
                    "52998224725", AdministratorRole.MANAGER);
            Category category = new Category("Electronics", null);
            entityManager.persist(administrator);
            entityManager.persist(category);

            Product product = new Product(
                    "Notebook", null, new BigDecimal("100.00"),
                    stock, administrator, category);
            entityManager.persist(product);

            List<CustomerData> customers = new ArrayList<>();
            for (int index = 1; index <= customerCount; index++) {
                Customer customer = customer(index);
                entityManager.persist(customer);
                Cart cart = new Cart(customer);
                cart.addProduct(product, 1);
                entityManager.persist(cart);
                customers.add(new CustomerData(customer.getId(), customer.getEmail()));
            }
            entityManager.flush();
            return new Fixture(product.getId(), List.copyOf(customers));
        });
    }

    private Customer customer(int index) {
        String suffix = String.format("%02d", index);
        return new Customer(
                "Customer " + index,
                "customer" + index + "@email.com",
                "hash",
                "123456789" + suffix,
                "119999900" + suffix,
                LocalDate.of(1990, 1, 1),
                new CustomerAddress(
                        "01310100", "Street", suffix, null,
                        "District", "City", "SP"));
    }

    private int productStock(java.util.UUID productId) {
        return jdbcTemplate.queryForObject(
                "select stock from products where id = ?", Integer.class, productId);
    }

    private long count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
    }

    private List<Outcome> successes(List<Outcome> outcomes) {
        return outcomes.stream().filter(outcome -> outcome.failure() == null).toList();
    }

    private List<Outcome> failures(List<Outcome> outcomes) {
        return outcomes.stream().filter(outcome -> outcome.failure() != null).toList();
    }

    private record Fixture(java.util.UUID productId, List<CustomerData> customers) {
    }

    private record CustomerData(java.util.UUID id, String email) {
    }

    private record Outcome(CheckoutCartUseCase.Result result, Throwable failure) {
    }
}
