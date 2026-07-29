package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.CustomerAddress;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.CartRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CartUseCasesTest {

    private CartRepositoryStub cartRepository;
    private CustomerRepositoryStub customerRepository;
    private ProductRepositoryStub productRepository;
    private AddProductToCartUseCase addUseCase;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer(
                "Customer",
                "customer@email.com",
                "hash",
                "52998224725",
                "11999998888",
                LocalDate.of(1990, 1, 1),
                new CustomerAddress("01310100", "Street", "10", null, "District", "City", "SP")
        );
        Product product = new Product(
                "Notebook", null, new BigDecimal("100.00"), 5, null, null);
        setId(customer, 1L);
        setId(product, 10L);

        cartRepository = new CartRepositoryStub();
        customerRepository = new CustomerRepositoryStub(customer);
        productRepository = new ProductRepositoryStub(product);
        addUseCase = new AddProductToCartUseCase(
                cartRepository, customerRepository, productRepository);
    }

    @Test
    void shouldAddAndConsolidateSameProduct() {
        addUseCase.execute(1L, 10L, 2);
        Cart cart = addUseCase.execute(1L, 10L, 3);

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getTotalItems());
        assertEquals(new BigDecimal("500.00"), cart.getTotal());
    }

    @Test
    void shouldRejectQuantityAboveAvailableStock() {
        assertThrows(BusinessRuleException.class,
                () -> addUseCase.execute(1L, 10L, 6));
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

    private static class CartRepositoryStub implements CartRepository {

        private Cart cart;

        @Override
        public Cart save(Cart cart) {
            this.cart = cart;
            return cart;
        }

        @Override
        public Optional<Cart> findActiveByCustomerId(Long customerId) {
            return Optional.ofNullable(cart);
        }
    }

    private record CustomerRepositoryStub(Customer customer) implements CustomerRepository {

        @Override
        public Customer save(Customer customer) {
            return customer;
        }

        @Override
        public boolean existsByEmail(String email) {
            return false;
        }

        @Override
        public boolean existsByCpf(String cpf) {
            return false;
        }

        @Override
        public Optional<Customer> findByIdForUpdate(Long id) {
            return Optional.of(customer);
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
            return Optional.of(product);
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
