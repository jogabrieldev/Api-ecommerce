package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomerQueryRepository {

    List<Customer> findAll();

    List<Purchase> findApprovedPurchases(java.util.UUID customerId);

    record Purchase(
            java.util.UUID orderId,
            LocalDateTime purchasedAt,
            java.util.UUID administratorId,
            String administratorName,
            java.util.UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) { }
}
