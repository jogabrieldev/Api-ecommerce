package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.application.usecase.CheckoutCartUseCase;

public record CheckoutResponse(OrderResponse order, PaymentResponse payment) {
    public static CheckoutResponse from(CheckoutCartUseCase.Result result) {
        return new CheckoutResponse(
                OrderResponse.from(result.order()),
                PaymentResponse.from(result.payment())
        );
    }
}
