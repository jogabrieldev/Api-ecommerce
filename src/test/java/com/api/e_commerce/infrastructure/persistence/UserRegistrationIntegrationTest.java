package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.application.usecase.CreateAdministratorUseCase;
import com.api.e_commerce.application.usecase.CreateCustomerUseCase;
import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class UserRegistrationIntegrationTest {

    @Autowired
    private CreateAdministratorUseCase createAdministratorUseCase;

    @Autowired
    private CreateCustomerUseCase createCustomerUseCase;

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
    void shouldRejectCustomerEmailAlreadyClaimedByAdministrator() {
        createInitialManager("manager@email.com", "52998224725");

        assertThrows(ConflictException.class,
                () -> createCustomer("manager@email.com", "11144477735"));

        assertEquals(1L, count("administrators"));
        assertEquals(0L, count("customers"));
        assertEquals(1L, count("user_identities"));
    }

    @Test
    void shouldRejectAdministratorCpfAlreadyClaimedByCustomer() {
        createCustomer("customer@email.com", "52998224725");

        assertThrows(ConflictException.class,
                () -> createInitialManager("manager@email.com", "52998224725"));

        assertEquals(0L, count("administrators"));
        assertEquals(1L, count("customers"));
        assertEquals(1L, count("user_identities"));
    }

    @Test
    void shouldCreateOnlyOneInitialManagerDuringConcurrentBootstrap() throws Exception {
        List<Outcome> outcomes = runConcurrently(
                () -> createInitialManager("manager-one@email.com", "52998224725"),
                () -> createInitialManager("manager-two@email.com", "11144477735"));

        assertEquals(1, outcomes.stream().filter(Outcome::successful).count());
        List<Throwable> failures = outcomes.stream()
                .map(Outcome::failure)
                .filter(failure -> failure != null)
                .toList();
        assertEquals(1, failures.size());
        assertInstanceOf(BusinessRuleException.class, failures.getFirst());
        assertEquals(1L, count("administrators"));
        assertEquals(1L, count("user_identities"));
    }

    private void createInitialManager(String email, String cpf) {
        createAdministratorUseCase.createInitialManager(
                "Initial Manager", email, "secure-password", cpf);
    }

    private void createCustomer(String email, String cpf) {
        createCustomerUseCase.execute(
                "Customer", email, "secure-password", cpf,
                "11999998888", LocalDate.of(1990, 1, 1),
                new CreateCustomerUseCase.AddressData(
                        "01310100", "Avenida Paulista", "1000", null,
                        "Bela Vista", "Sao Paulo", "SP"));
    }

    @SafeVarargs
    private List<Outcome> runConcurrently(ThrowingOperation... operations) throws Exception {
        CountDownLatch ready = new CountDownLatch(operations.length);
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(operations.length);
        try {
            List<Future<Outcome>> futures = new ArrayList<>();
            for (ThrowingOperation operation : operations) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await(5, TimeUnit.SECONDS);
                    try {
                        operation.run();
                        return new Outcome(null);
                    } catch (Throwable failure) {
                        return new Outcome(failure);
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

    private long count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Long.class);
    }

    private record Outcome(Throwable failure) {
        private boolean successful() {
            return failure == null;
        }
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws Exception;
    }
}
