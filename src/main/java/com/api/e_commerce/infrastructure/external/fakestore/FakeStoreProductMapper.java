package com.api.e_commerce.infrastructure.external.fakestore;

import com.api.e_commerce.domain.translation.TranslationService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FakeStoreProductMapper {
    private final TranslationService translationService;

    public FakeStoreProductMapper(TranslationService translationService) {
        this.translationService = translationService;
    }

    public MappedProduct map(FakeStoreProductResponse response) {
        if (response == null || response.id() == null || response.id() <= 0
                || response.title() == null || response.title().isBlank()
                || response.price() == null || response.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid Fake Store product payload");
        }
        String category = translationService.translateCategory(response.category());
        String name = truncate(translationService.translate(response.title()), 150);
        String description = truncate(translationService.translate(response.description()), 2000);
        String imageUrl = truncate(blankToNull(response.image()), 1000);
        int stock = response.rating() == null || response.rating().count() == null
                ? 0 : Math.max(0, response.rating().count());
        return new MappedProduct(
                response.id().toString(), name, description, response.price(), stock,
                category, imageUrl);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String truncate(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public record MappedProduct(
            String externalId,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String categoryName,
            String imageUrl
    ) {
    }
}
