package com.api.e_commerce.controller;

import com.api.e_commerce.model.Product;
import com.api.e_commerce.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateRequest request) {
        Product product = productService.create(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.administratorId(),
                request.categoryId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(product.getId()));
    }

    public record CreateRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 2000) String description,
            @NotNull @DecimalMin(value = "0.01") BigDecimal price,
            @NotNull @Min(0) Integer stock,
            @NotNull Long administratorId,
            @NotNull Long categoryId
    ) {
    }

    public record CreatedResponse(Long id) {
    }
}
