package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class FindProductByIdUseCase {

    private final ProductRepository productRepository;

    public FindProductByIdUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product execute(Long id) {
        return productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}
