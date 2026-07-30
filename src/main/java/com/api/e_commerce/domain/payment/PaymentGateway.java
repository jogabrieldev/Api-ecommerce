package com.api.e_commerce.domain.payment;

import com.api.e_commerce.domain.model.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGateway {

    Result charge(String token, PaymentMethod method, BigDecimal amount, String currency);

    record Result(boolean approved, String reference, String declineReason) {
    }
}
