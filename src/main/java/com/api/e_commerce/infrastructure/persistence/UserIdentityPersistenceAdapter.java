package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.exception.ConflictException;
import com.api.e_commerce.domain.model.IdentityType;
import com.api.e_commerce.domain.model.UserIdentity;
import com.api.e_commerce.domain.repository.UserIdentityRegistry;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserIdentityPersistenceAdapter implements UserIdentityRegistry {

    private final EntityManager entityManager;

    public UserIdentityPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void claim(String email, String cpf, IdentityType type) {
        if (existsByEmail(email)) {
            throw new ConflictException("E-mail already registered");
        }
        if (existsByCpf(cpf)) {
            throw new ConflictException("CPF already registered");
        }

        entityManager.persist(new UserIdentity(email, cpf, type));
        // The unique constraints arbitrate concurrent registrations.
        entityManager.flush();
    }

    private boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    private boolean existsByCpf(String cpf) {
        return count("cpf", cpf) > 0;
    }

    private long count(String field, String value) {
        return entityManager.createQuery(
                        "select count(identity) from UserIdentity identity where identity."
                                + field + " = :value",
                        Long.class)
                .setParameter("value", value)
                .getSingleResult();
    }
}
