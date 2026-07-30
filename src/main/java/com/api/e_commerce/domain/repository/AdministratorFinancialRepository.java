package com.api.e_commerce.domain.repository;

import java.math.BigDecimal;

public interface AdministratorFinancialRepository {

    Summary summarize(Long administratorId);

    record Summary(
            BigDecimal totalEarned,
            long unitsSold,
            long currentStock
    ) {
    }
}
