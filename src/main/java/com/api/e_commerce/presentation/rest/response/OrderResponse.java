package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.application.usecase.CheckoutCartUseCase;
import com.api.e_commerce.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        java.util.UUID id,
        java.util.UUID customerId,
        java.util.UUID cartId,
        OrderStatus status,
        List<ItemResponse> items,
        BigDecimal total,
        LocalDateTime createdAt
) {
    public static OrderResponse from(CheckoutCartUseCase.OrderData order) {
        return new OrderResponse(
                order.id(),
                order.customerId(),
                order.cartId(),
                order.status(),
                order.items().stream().map(ItemResponse::from).toList(),
                order.total(),
                order.createdAt()
        );
    }

    public record ItemResponse(
            java.util.UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        private static ItemResponse from(CheckoutCartUseCase.OrderItemData item) {
            return new ItemResponse(
                    item.productId(),
                    item.productName(),
                    item.quantity(),
                    item.unitPrice(),
                    item.subtotal()
            );
        }
    }
}
