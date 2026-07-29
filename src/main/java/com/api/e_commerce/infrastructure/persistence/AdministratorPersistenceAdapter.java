package com.api.e_commerce.infrastructure.persistence;

import com.api.e_commerce.domain.model.Administrator;
import com.api.e_commerce.domain.repository.AdministratorRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AdministratorPersistenceAdapter implements AdministratorRepository {

    private final EntityManager entityManager;

    public AdministratorPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Administrator save(Administrator administrator) {
        entityManager.persist(administrator);
        return administrator;
    }

    @Override
    public Optional<Administrator> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Administrator.class, id));
    }

    @Override
    public Optional<Administrator> findByEmail(String email) {
        return entityManager.createQuery(
                "select a from Administrator a where a.email = :email", Administrator.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Administrator> getUserAdm() {
        return entityManager.createQuery("select a from Administrator a order by a.id", Administrator.class).getResultList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return entityManager.createQuery(
                        "select count(a) from Administrator a where a.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0;
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return entityManager.createQuery(
                        "select count(a) from Administrator a where a.cpf = :cpf", Long.class)
                .setParameter("cpf", cpf)
                .getSingleResult() > 0;
    }
}
