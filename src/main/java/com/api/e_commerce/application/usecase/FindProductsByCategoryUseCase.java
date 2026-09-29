package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.CategoryRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FindProductsByCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public FindProductsByCategoryUseCase(CategoryRepository categoryRepository,
                                         ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Result execute(java.util.UUID categoryId, int page, int size) {
        categoryRepository.findById(categoryId)
                .filter(category -> Boolean.TRUE.equals(category.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        int offset = Math.multiplyExact(page, size);
        List<Product> products = productRepository.searchActive(null, categoryId, offset, size);
        long totalElements = productRepository.countActive(null, categoryId);
        int totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil((double) totalElements / size);

        return new Result(products, page, size, totalElements, totalPages);
    }

    public record Result(
            List<Product> products,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
