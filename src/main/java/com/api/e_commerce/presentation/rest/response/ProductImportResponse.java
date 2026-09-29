package com.api.e_commerce.presentation.rest.response;

import com.api.e_commerce.application.usecase.ImportFakeStoreProductsUseCase;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo da importação de produtos externos")
public record ProductImportResponse(
        @Schema(example = "20") int totalReceived,
        @Schema(example = "18") int totalImported,
        @Schema(example = "2") int totalUpdated,
        @Schema(example = "0") int totalErrors
) {
    public static ProductImportResponse from(ImportFakeStoreProductsUseCase.Result result) {
        return new ProductImportResponse(result.totalReceived(), result.totalImported(),
                result.totalUpdated(), result.totalErrors());
    }
}
