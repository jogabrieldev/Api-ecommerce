package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Order;
import com.api.e_commerce.domain.model.OrderItem;
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
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCart().getId(),
                order.getStatus(),
                order.getItems().stream().map(ItemResponse::from).toList(),
                order.getTotal(),
                order.getCreatedAt()
        );
    }

    public record ItemResponse(
            java.util.UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        private static ItemResponse from(OrderItem item) {
            return new ItemResponse(
                    item.getProduct().getId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
            );
        }
    }
}
