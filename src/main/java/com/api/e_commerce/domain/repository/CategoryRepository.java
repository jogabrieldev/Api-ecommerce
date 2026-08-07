package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Category;

import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(java.util.UUID id);

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
