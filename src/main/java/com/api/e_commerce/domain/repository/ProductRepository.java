package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.model.ProductSource;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    default Optional<Product> findBySourceAndExternalId(ProductSource source, String externalId) {
        return Optional.empty();
    }

    List<Product> findAllActive();

    Optional<Product> findActiveById(java.util.UUID id);

    Optional<Product> findActiveByIdForUpdate(java.util.UUID id);

    List<Product> searchActive(String name, java.util.UUID categoryId, int offset, int limit);

    long countActive(String name, java.util.UUID categoryId);
}
