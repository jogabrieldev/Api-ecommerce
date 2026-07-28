package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ForbiddenOperationException;
import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import com.api.e_commerce.domain.repository.CategoryRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CreateProductUseCase {

    private final ProductRepository productRepository;
    private final AdministratorRepository administratorRepository;
    private final CategoryRepository categoryRepository;

    public CreateProductUseCase(ProductRepository productRepository, AdministratorRepository administratorRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.administratorRepository = administratorRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product execute(String name, String description, BigDecimal price, Integer stock,
                           Long administratorId, Long categoryId, String authenticatedEmail) {
        Administrator administrator = administratorRepository.findById(administratorId)
                .orElseThrow(() -> new ResourceNotFoundException("Administrator not found"));
        if (!Boolean.TRUE.equals(administrator.getActive())) {
            throw new BusinessRuleException("Inactive administrator cannot register products");
        }
        if (!administrator.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new ForbiddenOperationException("Authenticated administrator cannot register products for another administrator");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new BusinessRuleException("Product cannot be registered in an inactive category");
        }

        if (price.signum() <= 0) {
            throw new BusinessRuleException("Product price must be greater than zero");
        }

        Product product = new Product(name.trim(), description, price, stock, administrator, category);
        return productRepository.save(product);
    }
}
