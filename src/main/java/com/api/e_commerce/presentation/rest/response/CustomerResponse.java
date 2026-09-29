package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Customer;

import java.time.LocalDateTime;

public record CustomerResponse(
        java.util.UUID id,
        String name,
        String maskedEmail,
        String maskedCpf,
        Boolean active,
        LocalDateTime createdAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                maskEmail(customer.getEmail()),
                maskCpf(customer.getCpf()),
                customer.getActive(),
                customer.getCreatedAt()
        );
    }

    private static String maskEmail(String email) {
        int separator = email.indexOf('@');
        if (separator <= 0) {
            return "***";
        }
        String localPart = email.substring(0, separator);
        String visible = localPart.substring(0, Math.min(2, localPart.length()));
        return visible + "***" + email.substring(separator);
    }

    private static String maskCpf(String cpf) {
        String digits = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digits.length() != 11) {
            return "***.***.***-**";
        }
        return "***.***.***-" + digits.substring(9);
    }
}
