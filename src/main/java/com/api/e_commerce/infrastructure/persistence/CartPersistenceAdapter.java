package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.CartStatus;
import com.api.e_commerce.domain.repository.CartRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CartPersistenceAdapter implements CartRepository {

    private final EntityManager entityManager;

    public CartPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Cart save(Cart cart) {
        if (cart.getId() == null) {
            entityManager.persist(cart);
        }
        entityManager.flush();
        return cart;
    }

    @Override
    public Optional<Cart> findActiveByCustomerId(Long customerId) {
        return findActiveByCustomerId(customerId, false);
    }

    @Override
    public Optional<Cart> findActiveByCustomerIdForUpdate(Long customerId) {
        return findActiveByCustomerId(customerId, true);
    }

    private Optional<Cart> findActiveByCustomerId(Long customerId, boolean lock) {
        var query = entityManager.createQuery(
                        """
                        select distinct c
                        from Cart c
                        left join fetch c.items i
                        left join fetch i.product
                        where c.customer.id = :customerId
                          and c.status = :status
                        """,
                        Cart.class
                )
                .setParameter("customerId", customerId)
                .setParameter("status", CartStatus.ACTIVE);
        if (lock) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }
        return query.getResultStream().findFirst();
    }
}
