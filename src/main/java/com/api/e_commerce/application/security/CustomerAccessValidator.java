package com.api.e_commerce.application.security;

import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ForbiddenOperationException;
import com.api.e_commerce.domain.model.Customer;

public final class CustomerAccessValidator {

    private CustomerAccessValidator() {}

    public static void validateOwner(Customer customer, String authenticatedEmail) {
        if (!Boolean.TRUE.equals(customer.getActive())) {
            throw new BusinessRuleException("Inactive customer cannot access the cart");
        }
        if (authenticatedEmail == null || !customer.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new ForbiddenOperationException("Authenticated customer cannot access another customer's cart");
        }
    }
}
