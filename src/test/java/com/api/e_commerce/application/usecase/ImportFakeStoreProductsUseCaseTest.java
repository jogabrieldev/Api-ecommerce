package com.api.e_commerce.application.usecase;

import com.api.e_commerce.application.translation.LocalProductTranslationService;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.AdministratorRole;
import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.model.ProductSource;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.repository.CategoryRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import com.api.e_commerce.infrastructure.external.fakestore.FakeStoreClient;
import com.api.e_commerce.infrastructure.external.fakestore.FakeStoreProductMapper;
import com.api.e_commerce.infrastructure.external.fakestore.FakeStoreProductResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportFakeStoreProductsUseCaseTest {
    @Test
    void shouldImportThenUpdateWithoutDuplication() {
        FakeStoreClient client = mock(FakeStoreClient.class);
        when(client.findAllProducts()).thenReturn(List.of(new FakeStoreProductResponse(
                1L, "Product", BigDecimal.TEN, "Description", "electronics", null, null)));
        Administrator administrator = new Administrator(
                "Admin", "admin@test.com", "hash", "52998224725", AdministratorRole.ADMIN);
        InMemoryProductRepository products = new InMemoryProductRepository();
        InMemoryCategoryRepository categories = new InMemoryCategoryRepository();
        AdministratorRepository administrators = mock(AdministratorRepository.class);
        when(administrators.findByEmail("admin@test.com")).thenReturn(Optional.of(administrator));
        var useCase = new ImportFakeStoreProductsUseCase(client,
                new FakeStoreProductMapper(new LocalProductTranslationService()),
                products, categories, administrators);

        var first = useCase.execute("admin@test.com");
        var second = useCase.execute("admin@test.com");

        assertEquals(1, first.totalImported());
        assertEquals(0, first.totalUpdated());
        assertEquals(0, second.totalImported());
        assertEquals(1, second.totalUpdated());
        assertEquals(1, products.entries.size());
        assertEquals(1, categories.entries.size());
    }

    private static class InMemoryProductRepository implements ProductRepository {
        private final Map<String, Product> entries = new HashMap<>();
        @Override public Product save(Product product) {
            entries.put(product.getSource() + ":" + product.getExternalId(), product);
            return product;
        }
        @Override public Optional<Product> findBySourceAndExternalId(ProductSource source, String externalId) {
            return Optional.ofNullable(entries.get(source + ":" + externalId));
        }
        @Override public List<Product> findAll() { return List.copyOf(entries.values()); }
        @Override public Optional<Product> findActiveById(java.util.UUID id) { return Optional.empty(); }
        @Override public Optional<Product> findActiveByIdForUpdate(java.util.UUID id) { return Optional.empty(); }
        @Override public List<Product> searchActive(String name, java.util.UUID categoryId, int offset, int limit) { return List.of(); }
        @Override public long countActive(String name, java.util.UUID categoryId) { return entries.size(); }
    }

    private static class InMemoryCategoryRepository implements CategoryRepository {
        private final Map<String, Category> entries = new HashMap<>();
        @Override public Category save(Category category) { entries.put(category.getName().toLowerCase(), category); return category; }
        @Override public Optional<Category> findById(java.util.UUID id) { return Optional.empty(); }
        @Override public Optional<Category> findByNameIgnoreCase(String name) { return Optional.ofNullable(entries.get(name.toLowerCase())); }
        @Override public List<Category> findAllActive() { return List.copyOf(entries.values()); }
        @Override public boolean existsByNameIgnoreCase(String name) { return entries.containsKey(name.toLowerCase()); }
    }
}
