package com.api.e_commerce.service;

import com.api.e_commerce.model.Administrator;
import com.api.e_commerce.model.Category;
import com.api.e_commerce.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final EntityManager entityManager;

    public ProductService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Product create(String name, String description, BigDecimal price, Integer stock,
                          Long administratorId, Long categoryId) {
        Administrator administrator = entityManager.find(Administrator.class, administratorId);
        if (administrator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrator not found");
        }
        if (!Boolean.TRUE.equals(administrator.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Inactive administrator cannot register products"
            );
        }

        Category category = entityManager.find(Category.class, categoryId);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Product cannot be registered in an inactive category"
            );
        }

        if (price.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "Product price must be greater than zero"
            );
        }

        Product product =
                new Product(name.trim(), description, price, stock, administrator, category);
        entityManager.persist(product);
        return product;
    }
}
