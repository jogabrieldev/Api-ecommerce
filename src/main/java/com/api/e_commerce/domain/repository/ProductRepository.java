package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    List<Product> findAll();

    Optional<Product> findActiveById(Long id);

    List<Product> searchActive(String name, Long categoryId, int offset, int limit);

    long countActive(String name, Long categoryId);
}
