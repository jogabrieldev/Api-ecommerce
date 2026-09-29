package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Payment;
import com.api.e_commerce.domain.repository.PaymentRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final EntityManager entityManager;

    public PaymentPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            entityManager.persist(payment);
        }
        entityManager.flush();
        return payment;
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        Optional<Payment> payment = entityManager.createQuery(
                        """
                        select distinct p
                        from Payment p
                        join fetch p.customer
                        join fetch p.cart
                        left join fetch p.order o
                        left join fetch o.customer
                        left join fetch o.cart
                        left join fetch p.allocations a
                        left join fetch a.administrator
                        where p.idempotencyKey = :idempotencyKey
                        """,
                        Payment.class)
                .setParameter("idempotencyKey", idempotencyKey)
                .getResultList()
                .stream()
                .findFirst();
        payment.map(Payment::getOrder).ifPresent(order -> entityManager.createQuery(
                        """
                        select distinct o
                        from Order o
                        left join fetch o.items i
                        left join fetch i.product
                        where o.id = :orderId
                        """,
                        com.api.e_commerce.domain.model.Order.class)
                .setParameter("orderId", order.getId())
                .getResultList());
        return payment;
    }
}
