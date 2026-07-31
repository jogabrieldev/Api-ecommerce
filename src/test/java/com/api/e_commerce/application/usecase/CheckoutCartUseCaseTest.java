package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ForbiddenOperationException;
import com.api.e_commerce.domain.exception.PaymentDeclinedException;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.CartStatus;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.CustomerAddress;
import com.api.e_commerce.domain.model.Order;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.model.Payment;
import com.api.e_commerce.domain.model.PaymentMethod;
import com.api.e_commerce.domain.model.PaymentStatus;
import com.api.e_commerce.domain.repository.PaymentRepository;
import com.api.e_commerce.domain.repository.CartRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.repository.OrderRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CheckoutCartUseCaseTest {

    private static final String IDEMPOTENCY_KEY = "123e4567-e89b-42d3-a456-426614174000";

    @Test
    void shouldCompleteOrderAndDecreaseStock() {
        Fixture fixture = fixture(5, 2);

        CheckoutCartUseCase.Result result = fixture.useCase.execute(
                1L, "customer@email.com", PaymentMethod.PIX,
                "SIM-APPROVED-123456789012", IDEMPOTENCY_KEY);
        Order order = result.order();

        assertEquals(3, fixture.product.getStock());
        assertEquals(CartStatus.COMPLETED, fixture.cart.getStatus());
        assertEquals(new BigDecimal("200.00"), order.getTotal());
        assertEquals(1, order.getItems().size());
        assertEquals(PaymentStatus.APPROVED, result.payment().getStatus());
        assertEquals(new BigDecimal("200.00"), result.payment().getAmount());
        assertEquals(new BigDecimal("200.00"),
                result.payment().getAllocations().getFirst().getAmount());
    }

    @Test
    void shouldRejectCheckoutFromAnotherCustomer() {
        Fixture fixture = fixture(5, 2);

        assertThrows(ForbiddenOperationException.class,
                () -> fixture.useCase.execute(
                        1L, "other@email.com", PaymentMethod.PIX,
                        "SIM-APPROVED-123456789012", IDEMPOTENCY_KEY));
        assertEquals(5, fixture.product.getStock());
        assertEquals(CartStatus.ACTIVE, fixture.cart.getStatus());
    }

    @Test
    void shouldRejectWhenStockChangedAfterItemWasAdded() {
        Fixture fixture = fixture(5, 4);
        fixture.product.setStock(3);

        assertThrows(BusinessRuleException.class,
                () -> fixture.useCase.execute(
                        1L, "customer@email.com", PaymentMethod.PIX,
                        "SIM-APPROVED-123456789012", IDEMPOTENCY_KEY));
        assertEquals(3, fixture.product.getStock());
        assertEquals(CartStatus.ACTIVE, fixture.cart.getStatus());
    }

    @Test
    void shouldNotDecreaseStockWhenPaymentIsDeclined() {
        Fixture fixture = fixture(5, 2);

        PaymentDeclinedException exception = assertThrows(
                PaymentDeclinedException.class,
                () -> fixture.useCase.execute(
                        1L, "customer@email.com", PaymentMethod.CREDIT_CARD,
                        "SIM-DECLINED-123456789012", IDEMPOTENCY_KEY));

        assertEquals(PaymentStatus.DECLINED, exception.getPayment().getStatus());
        assertEquals(5, fixture.product.getStock());
        assertEquals(CartStatus.ACTIVE, fixture.cart.getStatus());
    }

    @Test
    void shouldReturnSameOrderWithoutSecondStockReductionOnIdempotentRetry() {
        Fixture fixture = fixture(5, 2);
        CheckoutCartUseCase.Result first = fixture.useCase.execute(
                1L, "customer@email.com", PaymentMethod.PIX,
                "SIM-APPROVED-123456789012", IDEMPOTENCY_KEY);

        CheckoutCartUseCase.Result retry = fixture.useCase.execute(
                1L, "customer@email.com", PaymentMethod.PIX,
                "SIM-APPROVED-123456789012", IDEMPOTENCY_KEY);

        assertEquals(first.order(), retry.order());
        assertEquals(first.payment(), retry.payment());
        assertEquals(3, fixture.product.getStock());
    }

    private static Fixture fixture(int stock, int quantity) {
        Customer customer = new Customer(
                "Customer",
                "customer@email.com",
                "hash",
                "52998224725",
                "11999998888",
                LocalDate.of(1990, 1, 1),
                new CustomerAddress("01310100", "Street", "10", null, "District", "City", "SP")
        );
        Administrator administrator = new Administrator(
                "Seller", "seller@email.com", "hash",
                "11144477735", AdministratorRole.ADMIN);
        Product product = new Product(
                "Notebook", null, new BigDecimal("100.00"), stock, administrator, null);
        setId(customer, 1L);
        setId(administrator, 2L);
        setId(product, 10L);
        Cart cart = new Cart(customer);
        setId(cart, 20L);
        cart.addProduct(product, quantity);

        PaymentRepositoryStub paymentRepository = new PaymentRepositoryStub();
        CheckoutCartUseCase useCase = new CheckoutCartUseCase(
                new CustomerRepositoryStub(customer),
                new CartRepositoryStub(cart),
                new ProductRepositoryStub(product),
                order -> order,
                paymentRepository,
                (token, method, amount, currency) -> token.contains("APPROVED")
                        ? new com.api.e_commerce.domain.payment.PaymentGateway.Result(
                                true, "SIM-REFERENCE", null)
                        : new com.api.e_commerce.domain.payment.PaymentGateway.Result(
                                false, "SIM-REFERENCE", "Payment declined")
        );
        return new Fixture(useCase, cart, product);
    }

    private static class PaymentRepositoryStub implements PaymentRepository {
        private Payment payment;

        @Override
        public Payment save(Payment payment) {
            if (payment.getId() == null) {
                setId(payment, 30L);
            }
            this.payment = payment;
            return payment;
        }

        @Override
        public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
            return payment != null && payment.getIdempotencyKey().equals(idempotencyKey)
                    ? Optional.of(payment)
                    : Optional.empty();
        }
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }

    private record Fixture(CheckoutCartUseCase useCase, Cart cart, Product product) {
    }

    private record CustomerRepositoryStub(Customer customer) implements CustomerRepository {
        @Override
        public Customer save(Customer customer) {
            return customer;
        }

        @Override
        public boolean existsByEmail(String email) {
            return customer.getEmail().equals(email);
        }

        @Override
        public boolean existsByCpf(String cpf) {
            return customer.getCpf().equals(cpf);
        }

        @Override
        public Optional<Customer> findByEmail(String email) {
            return customer.getEmail().equals(email) ? Optional.of(customer) : Optional.empty();
        }

        @Override
        public Optional<Customer> findByIdForUpdate(Long id) {
            return customer.getId().equals(id) ? Optional.of(customer) : Optional.empty();
        }
    }

    private record CartRepositoryStub(Cart cart) implements CartRepository {
        @Override
        public Cart save(Cart cart) {
            return cart;
        }

        @Override
        public Optional<Cart> findActiveByCustomerId(Long customerId) {
            return cart.getStatus() == CartStatus.ACTIVE ? Optional.of(cart) : Optional.empty();
        }

        @Override
        public Optional<Cart> findActiveByCustomerIdForUpdate(Long customerId) {
            return findActiveByCustomerId(customerId);
        }
    }

    private record ProductRepositoryStub(Product product) implements ProductRepository {
        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public List<Product> findAll() {
            return List.of(product);
        }

        @Override
        public Optional<Product> findActiveById(Long id) {
            return product.getId().equals(id) && Boolean.TRUE.equals(product.getActive())
                    ? Optional.of(product)
                    : Optional.empty();
        }

        @Override
        public Optional<Product> findActiveByIdForUpdate(Long id) {
            return findActiveById(id);
        }

        @Override
        public List<Product> searchActive(String name, Long categoryId, int offset, int limit) {
            return List.of(product);
        }

        @Override
        public long countActive(String name, Long categoryId) {
            return 1;
        }
    }
}
