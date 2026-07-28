package com.api.e_commerce.service;

import com.api.e_commerce.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoryService {

    private final EntityManager entityManager;

    public CategoryService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Category create(String name, String description) {
        String normalizedName = name.trim();

        Long categoriesWithSameName = entityManager.createQuery(
                        "select count(c) from Category c where lower(c.name) = lower(:name)", Long.class)
                .setParameter("name", normalizedName)
                .getSingleResult();

        if (categoriesWithSameName > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category already registered");
        }

        Category category = new Category(normalizedName, description);
        entityManager.persist(category);
        return category;
    }
}
