package com.api.e_commerce.domain.exception;

import com.api.e_commerce.domain.model.Payment;

public class PaymentDeclinedException extends RuntimeException {

    private final Payment payment;

    public PaymentDeclinedException(Payment payment) {
        super(payment.getDeclineReason());
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }
}
