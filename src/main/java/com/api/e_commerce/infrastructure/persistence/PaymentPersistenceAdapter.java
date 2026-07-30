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
        return entityManager.createQuery(
                        """
                        select distinct p
                        from Payment p
                        left join fetch p.order
                        left join fetch p.allocations a
                        left join fetch a.administrator
                        where p.idempotencyKey = :idempotencyKey
                        """,
                        Payment.class)
                .setParameter("idempotencyKey", idempotencyKey)
                .getResultList()
                .stream()
                .findFirst();
    }
}
