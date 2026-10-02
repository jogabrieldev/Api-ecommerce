package com.api.e_commerce.infrastructure.payment;

import com.api.e_commerce.domain.model.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimulatedPaymentGatewayTest {

    private static final String KEY = "123e4567-e89b-42d3-a456-426614174000";

    @Test
    void shouldReturnTheSameChargeForAnIdempotentRetry() {
        SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();

        var first = gateway.charge(KEY, "SIM-APPROVED-123456789012",
                PaymentMethod.PIX, new BigDecimal("100.00"), "BRL");
        var retry = gateway.charge(KEY, "SIM-APPROVED-123456789012",
                PaymentMethod.PIX, new BigDecimal("100.00"), "BRL");

        assertEquals(first, retry);
    }

    @Test
    void shouldRejectTheSameKeyWithDifferentPaymentData() {
        SimulatedPaymentGateway gateway = new SimulatedPaymentGateway();
        gateway.charge(KEY, "SIM-APPROVED-123456789012",
                PaymentMethod.PIX, new BigDecimal("100.00"), "BRL");

        assertThrows(IllegalArgumentException.class, () -> gateway.charge(
                KEY, "SIM-DECLINED-123456789012",
                PaymentMethod.PIX, new BigDecimal("100.00"), "BRL"));
    }
}
