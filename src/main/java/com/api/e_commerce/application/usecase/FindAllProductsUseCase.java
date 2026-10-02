package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindAllProductsUseCase {

    private final ProductRepository productRepository;

    public FindAllProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> execute() {
        return productRepository.findAllActive();
    }
}
