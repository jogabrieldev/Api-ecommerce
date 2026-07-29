package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Customer;

import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<Customer> findByIdForUpdate(Long id);
}
