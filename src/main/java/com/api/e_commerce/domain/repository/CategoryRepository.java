package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Category;

import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);

    boolean existsByNameIgnoreCase(String name);
}
