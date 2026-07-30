package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Order;
import com.api.e_commerce.domain.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class OrderPersistenceAdapter implements OrderRepository {

    private final EntityManager entityManager;

    public OrderPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Order save(Order order) {
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }
}
