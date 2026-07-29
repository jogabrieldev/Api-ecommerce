package com.api.e_commerce.presentation.rest.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull @Min(1) Long productId,
        @NotNull @Min(1) Integer quantity
) {
}
