package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.model.ProductSource;
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
        if (product.getId() == null) {
            entityManager.persist(product);
            return product;
        }
        return entityManager.merge(product);
    }

    @Override
    public Optional<Product> findBySourceAndExternalId(ProductSource source, String externalId) {
        return entityManager.createQuery(
                "select p from Product p where p.source = :source and p.externalId = :externalId", Product.class)
                .setParameter("source", source)
                .setParameter("externalId", externalId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Product> findAllActive() {
        return entityManager.createQuery(
                        """
                        select p
                        from Product p
                        join fetch p.createdBy
                        join fetch p.category
                        where p.active = true
                          and p.category.active = true
                        order by p.id
                        """,
                        Product.class
                )
                .getResultList();
    }

    @Override
    public Optional<Product> findActiveById(java.util.UUID id) {
        return entityManager.createQuery(
                        """
                        select p
                        from Product p
                        join fetch p.createdBy
                        join fetch p.category
                        where p.id = :id
                          and p.active = true
                          and p.category.active = true
                        """,
                        Product.class
                )
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Product> findActiveByIdForUpdate(java.util.UUID id) {
        Optional<Product> product = entityManager.createQuery(
                        """
                        select p
                        from Product p
                        where p.id = :id
                          and p.active = true
                          and p.category.active = true
                        """,
                        Product.class)
                .setParameter("id", id)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultStream()
                .findFirst();
        product.ifPresent(value -> entityManager.refresh(value, LockModeType.PESSIMISTIC_WRITE));
        return product.filter(value -> Boolean.TRUE.equals(value.getActive())
                && Boolean.TRUE.equals(value.getCategory().getActive()));
    }

    @Override
    public List<Product> searchActive(String name, java.util.UUID categoryId, int offset, int limit) {
        return entityManager.createQuery(
                        """
                        select p
                        from Product p
                        join fetch p.createdBy
                        join fetch p.category
                        where p.active = true
                          and p.category.active = true
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
    public long countActive(String name, java.util.UUID categoryId) {
        return entityManager.createQuery(
                        """
                        select count(p)
                        from Product p
                        where p.active = true
                          and p.category.active = true
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
