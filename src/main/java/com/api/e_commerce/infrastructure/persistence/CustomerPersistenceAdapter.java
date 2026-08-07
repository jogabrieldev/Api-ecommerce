package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.repository.CustomerQueryRepository;
import com.api.e_commerce.domain.model.PaymentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerPersistenceAdapter implements CustomerRepository, CustomerQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Customer save(Customer customer) {
        entityManager.persist(customer);
        entityManager.flush();
        return customer;
    }

    @Override
    public boolean existsByEmail(String email) {
        return countBy("email", email) > 0;
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return countBy("cpf", cpf) > 0;
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return entityManager.createQuery(
                        "select c from Customer c where c.email = :email",
                        Customer.class)
                .setParameter("email", email)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Customer> findByIdForUpdate(java.util.UUID id) {
        return Optional.ofNullable(entityManager.find(Customer.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

    @Override
    public List<Customer> findAll() {
        return entityManager.createQuery(
                        "select c from Customer c order by c.name, c.id", Customer.class)
                .getResultList();
    }

    @Override
    public List<CustomerQueryRepository.Purchase> findApprovedPurchases(java.util.UUID customerId) {
        return entityManager.createQuery(
                        """
                        select new com.api.e_commerce.domain.repository.CustomerQueryRepository$Purchase(
                            o.id, o.createdAt, a.id, a.name, product.id, oi.productName,
                            oi.quantity, oi.unitPrice, (oi.unitPrice * oi.quantity)
                        )
                        from Payment payment
                        join payment.order o
                        join o.items oi
                        join oi.product product
                        join product.createdBy a
                        where payment.customer.id = :customerId
                          and payment.status = :status
                        order by o.createdAt desc, o.id desc, oi.id
                        """,
                        CustomerQueryRepository.Purchase.class)
                .setParameter("customerId", customerId)
                .setParameter("status", PaymentStatus.APPROVED)
                .getResultList();
    }

    private long countBy(String field, String value) {
        return entityManager.createQuery(
                        "select count(c) from Customer c where c." + field + " = :value", Long.class)
                .setParameter("value", value)
                .getSingleResult();
    }
}
