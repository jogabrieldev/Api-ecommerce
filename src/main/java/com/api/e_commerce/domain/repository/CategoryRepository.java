package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(java.util.UUID id);

    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findAllActive();

    boolean existsByNameIgnoreCase(String name);
}
