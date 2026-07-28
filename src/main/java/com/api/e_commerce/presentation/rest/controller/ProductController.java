package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.CreateProductUseCase;
import com.api.e_commerce.application.usecase.FindAllProductsUseCase;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.presentation.rest.request.CreateProductRequest;
import com.api.e_commerce.presentation.rest.response.CreatedResponse;
import com.api.e_commerce.presentation.rest.response.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final FindAllProductsUseCase findAllProductsUseCase;

    public ProductController(CreateProductUseCase createProductUseCase, FindAllProductsUseCase findAllProductsUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.findAllProductsUseCase = findAllProductsUseCase;
    }

    @PostMapping
    public ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateProductRequest request, Authentication authentication) {
        Product product = createProductUseCase.execute(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                request.administratorId(),
                request.categoryId(),
                authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(product.getId(), product.getName()));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        List<ProductResponse> products = findAllProductsUseCase.execute()
                .stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(products);
    }
}
