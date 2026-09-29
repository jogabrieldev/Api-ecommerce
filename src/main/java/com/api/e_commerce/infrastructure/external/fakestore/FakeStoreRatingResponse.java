package com.api.e_commerce.infrastructure.external.fakestore;

import java.math.BigDecimal;

public record FakeStoreRatingResponse(BigDecimal rate, Integer count) {
}
