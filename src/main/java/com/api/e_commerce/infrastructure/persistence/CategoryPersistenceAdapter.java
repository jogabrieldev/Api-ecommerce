package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.domain.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CategoryPersistenceAdapter implements CategoryRepository {

    private final EntityManager entityManager;

    public CategoryPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Category save(Category category) {
        entityManager.persist(category);
        return category;
    }

    @Override
    public Optional<Category> findById(java.util.UUID id) {
        return Optional.ofNullable(entityManager.find(Category.class, id));
    }

    @Override
    public Optional<Category> findByNameIgnoreCase(String name) {
        return entityManager.createQuery(
                        "select c from Category c where lower(c.name) = lower(:name)", Category.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Category> findAllActive() {
        return entityManager.createQuery(
                        "select c from Category c where c.active = true order by c.name, c.id",
                        Category.class)
                .getResultList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return entityManager.createQuery(
                        "select count(c) from Category c where lower(c.name) = lower(:name)", Long.class)
                .setParameter("name", name)
                .getSingleResult() > 0;
    }
}
