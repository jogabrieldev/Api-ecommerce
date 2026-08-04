package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindCustomerByEmailUseCase {

    private final CustomerRepository customerRepository;

    public FindCustomerByEmailUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public Customer execute(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return customerRepository.findByEmail(normalizedEmail).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
}
