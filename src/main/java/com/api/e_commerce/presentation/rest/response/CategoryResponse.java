package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Category;

public record CategoryResponse(
        java.util.UUID id,
        String name,
        String description
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
