package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductSearchUseCasesTest {

    @Test
    void shouldFindActiveProductById() {
        Product expected = product();
        ProductRepositoryStub repository = new ProductRepositoryStub();
        repository.product = expected;

        Product result = new FindProductByIdUseCase(repository).execute(10L);

        assertSame(expected, result);
        assertEquals(10L, repository.requestedId);
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        ProductRepositoryStub repository = new ProductRepositoryStub();

        assertThrows(ResourceNotFoundException.class,
                () -> new FindProductByIdUseCase(repository).execute(10L));
    }

    @Test
    void shouldSearchWithFiltersAndPagination() {
        ProductRepositoryStub repository = new ProductRepositoryStub();
        repository.products = List.of(product(), product());
        repository.total = 22;

        SearchProductsUseCase.Result result =
                new SearchProductsUseCase(repository).execute(" Notebook ", 3L, 1, 10);

        assertEquals("Notebook", repository.name);
        assertEquals(3L, repository.categoryId);
        assertEquals(10, repository.offset);
        assertEquals(10, repository.limit);
        assertEquals(22, result.totalElements());
        assertEquals(3, result.totalPages());
    }

    private static Product product() {
        return new Product("Notebook", null, BigDecimal.ONE, 1, null, null);
    }

    private static class ProductRepositoryStub implements ProductRepository {

        private Product product;
        private List<Product> products = List.of();
        private long total;
        private Long requestedId;
        private String name;
        private Long categoryId;
        private int offset;
        private int limit;

        @Override
        public Product save(Product product) {
            return product;
        }

        @Override
        public List<Product> findAll() {
            return products;
        }

        @Override
        public Optional<Product> findActiveById(Long id) {
            requestedId = id;
            return Optional.ofNullable(product);
        }

        @Override
        public Optional<Product> findActiveByIdForUpdate(Long id) {
            return findActiveById(id);
        }

        @Override
        public List<Product> searchActive(String name, Long categoryId, int offset, int limit) {
            this.name = name;
            this.categoryId = categoryId;
            this.offset = offset;
            this.limit = limit;
            return products;
        }

        @Override
        public long countActive(String name, Long categoryId) {
            return total;
        }
    }
}
