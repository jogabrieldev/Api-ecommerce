package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.repository.CustomerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerPersistenceAdapter implements CustomerRepository {

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
    public Optional<Customer> findByIdForUpdate(Long id) {
        return Optional.ofNullable(entityManager.find(Customer.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

    private long countBy(String field, String value) {
        return entityManager.createQuery(
                        "select count(c) from Customer c where c." + field + " = :value", Long.class)
                .setParameter("value", value)
                .getSingleResult();
    }
}
