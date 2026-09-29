package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.application.usecase.CheckoutCartUseCase;
import com.api.e_commerce.domain.model.PaymentMethod;
import com.api.e_commerce.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentResponse(
        java.util.UUID id,
        PaymentStatus status,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        String gatewayReference,
        String idempotencyKey,
        List<AllocationResponse> allocations,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(CheckoutCartUseCase.PaymentData payment) {
        return new PaymentResponse(
                payment.id(),
                payment.status(),
                payment.method(),
                payment.amount(),
                payment.currency(),
                payment.gatewayReference(),
                payment.idempotencyKey(),
                payment.allocations().stream().map(AllocationResponse::from).toList(),
                payment.createdAt()
        );
    }

    public record AllocationResponse(
            java.util.UUID administratorId,
            String administratorName,
            BigDecimal amount
    ) {
        private static AllocationResponse from(CheckoutCartUseCase.AllocationData allocation) {
            return new AllocationResponse(
                    allocation.administratorId(),
                    allocation.administratorName(),
                    allocation.amount()
            );
        }
    }
}
