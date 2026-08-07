package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchProductsUseCase {

    private final ProductRepository productRepository;

    public SearchProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Result execute(String name, java.util.UUID categoryId, int page, int size) {
        String normalizedName = name == null || name.isBlank() ? null : name.trim();
        int offset = page * size;
        List<Product> products = productRepository.searchActive(normalizedName, categoryId, offset, size);
        long totalElements = productRepository.countActive(normalizedName, categoryId);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

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
