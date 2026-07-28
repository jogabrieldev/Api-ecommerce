package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
