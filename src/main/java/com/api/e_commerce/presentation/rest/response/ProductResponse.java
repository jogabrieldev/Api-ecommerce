package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.domain.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Produto persistido no catálogo interno")
public record ProductResponse(
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000") java.util.UUID id,
        @Schema(example = "Camiseta Masculina Premium") String name,
        @Schema(example = "Descrição original preservada quando não há tradutor dinâmico") String description,
        @Schema(example = "109.90") BigDecimal price,
        @Schema(example = "120") Integer stock,
        Boolean active,
        @Schema(example = "1") String externalId,
        @Schema(example = "FAKE_STORE") String source,
        @Schema(example = "https://fakestoreapi.com/img/product.png") String imageUrl,
        java.util.UUID administratorId,
        java.util.UUID categoryId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getExternalId(),
                product.getSource().name(),
                product.getImageUrl(),
                product.getCreatedBy().getId(),
                product.getCategory().getId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
