package com.api.e_commerce.infrastructure.external.fakestore;

import java.math.BigDecimal;

public record FakeStoreProductResponse(
        Long id,
        String title,
        BigDecimal price,
        String description,
        String category,
        String image,
        FakeStoreRatingResponse rating
) {
}
