package com.api.e_commerce.infrastructure.payment;

import com.api.e_commerce.domain.model.PaymentMethod;
import com.api.e_commerce.domain.payment.PaymentGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final Pattern APPROVED_TOKEN =
            Pattern.compile("SIM-APPROVED-[A-Za-z0-9]{12,40}");
    private static final Pattern DECLINED_TOKEN =
            Pattern.compile("SIM-DECLINED-[A-Za-z0-9]{12,40}");
    private final ConcurrentMap<String, ProcessedCharge> processedCharges = new ConcurrentHashMap<>();

    @Override
    public Result charge(String idempotencyKey, String token, PaymentMethod method,
                         BigDecimal amount, String currency) {
        validateCharge(method, amount, currency);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        ChargeRequest request = new ChargeRequest(token, method, amount, currency);
        ProcessedCharge processed = processedCharges.compute(idempotencyKey, (key, existing) -> {
            if (existing != null) {
                if (!existing.request().equals(request)) {
                    throw new IllegalArgumentException(
                            "Idempotency key was already used with different payment data");
                }
                return existing;
            }
            return new ProcessedCharge(request, process(key, token));
        });
        return processed.result();
    }

    private Result process(String idempotencyKey, String token) {
        String reference = "SIM-" + UUID.nameUUIDFromBytes(
                idempotencyKey.getBytes(StandardCharsets.UTF_8));

        if (APPROVED_TOKEN.matcher(token).matches()) {
            return new Result(true, reference, null);
        }
        if (DECLINED_TOKEN.matcher(token).matches()) {
            return new Result(false, reference, "Payment declined by simulated issuer");
        }
        return new Result(false, reference, "Invalid simulated payment token");
    }

    private void validateCharge(PaymentMethod method, BigDecimal amount, String currency) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException("Payment amount must be positive with at most 2 decimal places");
        }
        if (!"BRL".equals(currency)) {
            throw new IllegalArgumentException("Only BRL is supported");
        }
    }

    private record ChargeRequest(String token, PaymentMethod method,
                                 BigDecimal amount, String currency) {
    }

    private record ProcessedCharge(ChargeRequest request, Result result) {
    }
}
