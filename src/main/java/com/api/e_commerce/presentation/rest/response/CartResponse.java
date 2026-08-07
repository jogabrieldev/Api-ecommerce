package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.CartItem;
import com.api.e_commerce.domain.model.CartStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        java.util.UUID id,
        java.util.UUID customerId,
        CartStatus status,
        List<ItemResponse> items,
        int totalItems,
        BigDecimal total,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CartResponse from(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getCustomer().getId(),
                cart.getStatus(),
                cart.getItems().stream().map(ItemResponse::from).toList(),
                cart.getTotalItems(),
                cart.getTotal(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    public record ItemResponse(
            java.util.UUID id,
            java.util.UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            Integer availableStock
    ) {

        private static ItemResponse from(CartItem item) {
            return new ItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal(),
                    item.getProduct().getStock()
            );
        }
    }
}
