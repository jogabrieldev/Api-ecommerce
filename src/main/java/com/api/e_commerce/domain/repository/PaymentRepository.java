package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
