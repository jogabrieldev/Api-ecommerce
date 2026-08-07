package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Administrator;

import java.util.List;
import java.util.Optional;

public interface AdministratorRepository {

    Administrator save(Administrator administrator);

    List<Administrator> getUserAdm();

    Optional<Administrator> findById(java.util.UUID id);

    Optional<Administrator> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);
}
