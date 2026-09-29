package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.model.ProductSource;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.repository.CategoryRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import com.api.e_commerce.infrastructure.external.fakestore.FakeStoreClient;
import com.api.e_commerce.infrastructure.external.fakestore.FakeStoreProductMapper;
import com.api.e_commerce.infrastructure.external.fakestore.FakeStoreProductResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImportFakeStoreProductsUseCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportFakeStoreProductsUseCase.class);
    private final FakeStoreClient fakeStoreClient;
    private final FakeStoreProductMapper mapper;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AdministratorRepository administratorRepository;

    public ImportFakeStoreProductsUseCase(
            FakeStoreClient fakeStoreClient,
            FakeStoreProductMapper mapper,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            AdministratorRepository administratorRepository) {
        this.fakeStoreClient = fakeStoreClient;
        this.mapper = mapper;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.administratorRepository = administratorRepository;
    }

    @Transactional
    public Result execute(String authenticatedEmail) {
        Administrator administrator = administratorRepository.findByEmail(authenticatedEmail)
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Active administrator not found"));
        List<FakeStoreProductResponse> externalProducts = fakeStoreClient.findAllProducts();
        int imported = 0;
        int updated = 0;
        int errors = 0;
        for (FakeStoreProductResponse externalProduct : externalProducts) {
            try {
                FakeStoreProductMapper.MappedProduct data = mapper.map(externalProduct);
                Category category = categoryRepository.findByNameIgnoreCase(data.categoryName())
                        .orElseGet(() -> categoryRepository.save(new Category(
                                data.categoryName(), "Categoria criada pela importação da Fake Store API")));
                var existing = productRepository.findBySourceAndExternalId(ProductSource.FAKE_STORE, data.externalId());
                if (existing.isPresent()) {
                    existing.get().updateExternalData(data.name(), data.description(), data.price(),
                            data.stock(), category, data.imageUrl());
                    productRepository.save(existing.get());
                    updated++;
                } else {
                    productRepository.save(new Product(
                            data.name(), data.description(), data.price(), data.stock(), administrator,
                            category, data.externalId(), ProductSource.FAKE_STORE, data.imageUrl()));
                    imported++;
                }
            } catch (IllegalArgumentException exception) {
                errors++;
                LOGGER.warn("Fake Store product skipped during import: {}", exception.getMessage());
            }
        }
        return new Result(externalProducts.size(), imported, updated, errors);
    }

    public record Result(int totalReceived, int totalImported, int totalUpdated, int totalErrors) {
    }
}
