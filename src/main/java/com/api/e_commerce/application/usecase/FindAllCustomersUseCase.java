package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.repository.CustomerQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FindAllCustomersUseCase {

    private final CustomerQueryRepository customerQueryRepository;

    public FindAllCustomersUseCase(CustomerQueryRepository customerQueryRepository) {
        this.customerQueryRepository = customerQueryRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> execute() {
        return customerQueryRepository.findAll();
    }
}
