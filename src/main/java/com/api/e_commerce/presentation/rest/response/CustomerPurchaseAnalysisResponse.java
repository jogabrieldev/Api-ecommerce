package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.application.usecase.AnalyzeCustomerPurchasesUseCase;
import com.api.e_commerce.domain.repository.CustomerQueryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CustomerPurchaseAnalysisResponse(
        CustomerResponse customer,
        boolean hasPurchasedFromAdministrator,
        List<PurchaseResponse> purchases
) {
    public static CustomerPurchaseAnalysisResponse from(
            AnalyzeCustomerPurchasesUseCase.Result result) {
        return new CustomerPurchaseAnalysisResponse(
                CustomerResponse.from(result.customer()),
                result.hasPurchasedFromAdministrator(),
                result.purchases().stream().map(PurchaseResponse::from).toList()
        );
    }

    public record PurchaseResponse(
            java.util.UUID orderId,
            LocalDateTime purchasedAt,
            AdministratorResponse administrator,
            ProductResponse product,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        private static PurchaseResponse from(CustomerQueryRepository.Purchase purchase) {
            return new PurchaseResponse(
                    purchase.orderId(),
                    purchase.purchasedAt(),
                    new AdministratorResponse(
                            purchase.administratorId(), purchase.administratorName()),
                    new ProductResponse(purchase.productId(), purchase.productName()),
                    purchase.quantity(),
                    purchase.unitPrice(),
                    purchase.subtotal()
            );
        }
    }

    public record AdministratorResponse(java.util.UUID id, String name) {
    }

    public record ProductResponse(java.util.UUID id, String name) {
    }
}
