package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Payment;
import com.api.e_commerce.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentDeclinedResponse(
        int status,
        String error,
        String message,
        java.util.UUID paymentId,
        PaymentStatus paymentStatus,
        BigDecimal amount,
        String currency,
        LocalDateTime timestamp
) {
    public static PaymentDeclinedResponse from(Payment payment) {
        return new PaymentDeclinedResponse(
                402,
                "Payment Required",
                payment.getDeclineReason(),
                payment.getId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                LocalDateTime.now()
        );
    }
}
