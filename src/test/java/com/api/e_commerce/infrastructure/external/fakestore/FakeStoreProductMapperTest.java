package com.api.e_commerce.infrastructure.external.fakestore;

import com.api.e_commerce.application.translation.LocalProductTranslationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FakeStoreProductMapperTest {
    private final FakeStoreProductMapper mapper =
            new FakeStoreProductMapper(new LocalProductTranslationService());

    @Test
    void shouldMapAndNormalizeExternalProduct() {
        var mapped = mapper.map(new FakeStoreProductResponse(
                1L, " Product ", new BigDecimal("109.95"), " Description ",
                "electronics", " https://example.com/image.png ",
                new FakeStoreRatingResponse(new BigDecimal("3.9"), 120)));

        assertEquals("1", mapped.externalId());
        assertEquals("Product", mapped.name());
        assertEquals("Eletrônicos", mapped.categoryName());
        assertEquals(120, mapped.stock());
    }

    @Test
    void shouldRejectInvalidPayload() {
        assertThrows(IllegalArgumentException.class, () -> mapper.map(
                new FakeStoreProductResponse(null, null, null, null, null, null, null)));
    }
}
