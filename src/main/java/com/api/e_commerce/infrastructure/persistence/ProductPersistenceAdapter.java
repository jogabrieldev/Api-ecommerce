package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class ProductPersistenceAdapter implements ProductRepository {

    private final EntityManager entityManager;

    public ProductPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Product save(Product product) {
        entityManager.persist(product);
        return product;
    }

    @Override
    public List<Product> findAll() {
        return entityManager.createQuery(
                        """
                        select p
                        from Product p
                        join fetch p.createdBy
                        join fetch p.category
                        order by p.id
                        """,
                        Product.class
                )
                .getResultList();
    }

    @Override
    public Optional<Product> findActiveById(Long id) {
        return entityManager.createQuery(
                        """
                        select p
                        from Product p
                        join fetch p.createdBy
                        join fetch p.category
                        where p.id = :id
                          and p.active = true
                        """,
                        Product.class
                )
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Product> findActiveByIdForUpdate(Long id) {
        return entityManager.createQuery(
                        """
                        select p
                        from Product p
                        where p.id = :id
                          and p.active = true
                        """,
                        Product.class)
                .setParameter("id", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Product> searchActive(String name, Long categoryId, int offset, int limit) {
        return entityManager.createQuery(
                        """
                        select p
                        from Product p
                        join fetch p.createdBy
                        join fetch p.category
                        where p.active = true
                          and (:name is null or lower(p.name) like :name)
                          and (:categoryId is null or p.category.id = :categoryId)
                        order by p.name, p.id
                        """,
                        Product.class
                )
                .setParameter("name", nameParameter(name))
                .setParameter("categoryId", categoryId)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public long countActive(String name, Long categoryId) {
        return entityManager.createQuery(
                        """
                        select count(p)
                        from Product p
                        where p.active = true
                          and (:name is null or lower(p.name) like :name)
                          and (:categoryId is null or p.category.id = :categoryId)
                        """,
                        Long.class
                )
                .setParameter("name", nameParameter(name))
                .setParameter("categoryId", categoryId)
                .getSingleResult();
    }

    private String nameParameter(String name) {
        return name == null ? null : "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
    }
}
