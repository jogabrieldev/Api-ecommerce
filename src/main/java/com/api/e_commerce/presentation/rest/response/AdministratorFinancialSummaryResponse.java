package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.repository.AdministratorFinancialRepository;

import java.math.BigDecimal;

public record AdministratorFinancialSummaryResponse(
        Long administratorId,
        BigDecimal totalEarned,
        String currency,
        long unitsSold,
        long currentStock
) {
    public static AdministratorFinancialSummaryResponse from(
            Long administratorId,
            AdministratorFinancialRepository.Summary summary) {
        return new AdministratorFinancialSummaryResponse(
                administratorId,
                summary.totalEarned(),
                "BRL",
                summary.unitsSold(),
                summary.currentStock()
        );
    }
}
