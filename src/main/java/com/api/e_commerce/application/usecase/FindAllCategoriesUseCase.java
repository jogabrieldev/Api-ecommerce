package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.domain.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FindAllCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public FindAllCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> execute() {
        return categoryRepository.findAllActive();
    }
}
