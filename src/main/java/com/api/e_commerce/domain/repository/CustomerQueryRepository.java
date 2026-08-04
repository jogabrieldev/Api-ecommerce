package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CustomerQueryRepository {

    List<Customer> findAll();

    List<Purchase> findApprovedPurchases(Long customerId);

    record Purchase(
            Long orderId,
            LocalDateTime purchasedAt,
            Long administratorId,
            String administratorName,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) { }
}
