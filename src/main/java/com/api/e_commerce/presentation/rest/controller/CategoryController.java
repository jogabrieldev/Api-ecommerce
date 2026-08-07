package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.CreateCategoryUseCase;
import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.presentation.rest.request.CreateCategoryRequest;
import com.api.e_commerce.presentation.rest.response.CreatedResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categorias", description = "Categorias relacionais do catálogo")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar categoria")
    public ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = createCategoryUseCase.execute(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(category.getId(), category.getName()));
    }
}
