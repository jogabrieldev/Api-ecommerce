package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Customer;

public record CustomerTokenResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        Long customerId,
        String name,
        String email
) {
    public static CustomerTokenResponse from(
            Customer customer, String accessToken, long expiresIn) {
        return new CustomerTokenResponse(
                "Bearer",
                accessToken,
                expiresIn,
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }
}
