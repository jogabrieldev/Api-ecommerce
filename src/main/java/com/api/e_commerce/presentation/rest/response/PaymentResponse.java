package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Payment;
import com.api.e_commerce.domain.model.PaymentAllocation;
import com.api.e_commerce.domain.model.PaymentMethod;
import com.api.e_commerce.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentResponse(
        Long id,
        PaymentStatus status,
        PaymentMethod method,
        BigDecimal amount,
        String currency,
        String gatewayReference,
        String idempotencyKey,
        List<AllocationResponse> allocations,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getGatewayReference(),
                payment.getIdempotencyKey(),
                payment.getAllocations().stream().map(AllocationResponse::from).toList(),
                payment.getCreatedAt()
        );
    }

    public record AllocationResponse(
            Long administratorId,
            String administratorName,
            BigDecimal amount
    ) {
        private static AllocationResponse from(PaymentAllocation allocation) {
            return new AllocationResponse(
                    allocation.getAdministrator().getId(),
                    allocation.getAdministrator().getName(),
                    allocation.getAmount()
            );
        }
    }
}
