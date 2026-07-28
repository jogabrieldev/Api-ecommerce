package com.api.e_commerce.service;

import com.api.e_commerce.model.Administrator;
import com.api.e_commerce.model.AdministratorRole;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdministratorService {

    private final EntityManager entityManager;

    public AdministratorService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Administrator create(String name, String email, String passwordHash, String cpf,
                                AdministratorRole role) {
        String normalizedEmail = email.trim().toLowerCase();

        if (existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail already registered");
        }
        if (existsByCpf(cpf)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF already registered");
        }

        Administrator administrator =
                new Administrator(name.trim(), normalizedEmail, passwordHash, cpf, role);
        entityManager.persist(administrator);
        return administrator;
    }

    private boolean existsByEmail(String email) {
        return entityManager.createQuery(
                        "select count(a) from Administrator a where a.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0;
    }

    private boolean existsByCpf(String cpf) {
        return entityManager.createQuery(
                        "select count(a) from Administrator a where a.cpf = :cpf", Long.class)
                .setParameter("cpf", cpf)
                .getSingleResult() > 0;
    }
}
