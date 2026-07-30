package com.api.e_commerce.presentation.rest.request;

import com.api.e_commerce.domain.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CheckoutRequest(
        @NotNull PaymentMethod paymentMethod,
        @NotBlank
        @Pattern(
                regexp = "SIM-(?:APPROVED|DECLINED)-[A-Za-z0-9]{12,40}",
                message = "Use a valid simulated token")
        String paymentToken,
        @NotBlank
        @Pattern(
                regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}",
                message = "Idempotency key must be a valid UUID")
        String idempotencyKey
) {
}
