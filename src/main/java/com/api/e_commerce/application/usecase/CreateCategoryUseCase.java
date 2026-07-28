package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ConflictException;
import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category execute(String name, String description) {
        String normalizedName = name.trim();

        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("Category already registered");
        }

        return categoryRepository.save(new Category(normalizedName, description));
    }
}
