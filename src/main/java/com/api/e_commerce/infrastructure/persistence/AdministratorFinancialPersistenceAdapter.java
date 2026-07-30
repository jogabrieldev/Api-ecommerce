package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.PaymentStatus;
import com.api.e_commerce.domain.repository.AdministratorFinancialRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class AdministratorFinancialPersistenceAdapter
        implements AdministratorFinancialRepository {

    private final EntityManager entityManager;

    public AdministratorFinancialPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Summary summarize(Long administratorId) {
        BigDecimal totalEarned = entityManager.createQuery(
                        """
                        select coalesce(sum(a.amount), 0)
                        from PaymentAllocation a
                        where a.administrator.id = :administratorId
                          and a.payment.status = :status
                        """,
                        BigDecimal.class)
                .setParameter("administratorId", administratorId)
                .setParameter("status", PaymentStatus.APPROVED)
                .getSingleResult();

        Long unitsSold = entityManager.createQuery(
                        """
                        select coalesce(sum(oi.quantity), 0)
                        from Payment p
                        join p.order o
                        join o.items oi
                        where p.status = :status
                          and oi.product.createdBy.id = :administratorId
                        """,
                        Long.class)
                .setParameter("administratorId", administratorId)
                .setParameter("status", PaymentStatus.APPROVED)
                .getSingleResult();

        Long currentStock = entityManager.createQuery(
                        """
                        select coalesce(sum(p.stock), 0)
                        from Product p
                        where p.createdBy.id = :administratorId
                        """,
                        Long.class)
                .setParameter("administratorId", administratorId)
                .getSingleResult();

        return new Summary(totalEarned, unitsSold, currentStock);
    }
}
