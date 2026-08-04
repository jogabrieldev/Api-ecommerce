package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.repository.CustomerQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalyzeCustomerPurchasesUseCase {

    private final FindCustomerByEmailUseCase findCustomerByEmailUseCase;
    private final CustomerQueryRepository customerQueryRepository;

    public AnalyzeCustomerPurchasesUseCase(FindCustomerByEmailUseCase findCustomerByEmailUseCase, CustomerQueryRepository customerQueryRepository) {
        this.findCustomerByEmailUseCase = findCustomerByEmailUseCase;
        this.customerQueryRepository = customerQueryRepository;
    }

    @Transactional(readOnly = true)
    public Result execute(String email) {
        Customer customer = findCustomerByEmailUseCase.execute(email);
        List<CustomerQueryRepository.Purchase> purchases = customerQueryRepository.findApprovedPurchases(customer.getId());
        return new Result(customer, !purchases.isEmpty(), purchases);
    }

    public record Result(
            Customer customer,
            boolean hasPurchasedFromAdministrator,
            List<CustomerQueryRepository.Purchase> purchases
    ) {
    }
}
