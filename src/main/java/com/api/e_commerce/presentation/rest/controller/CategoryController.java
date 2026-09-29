package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.CreateCategoryUseCase;
import com.api.e_commerce.application.usecase.FindAllCategoriesUseCase;
import com.api.e_commerce.application.usecase.FindProductsByCategoryUseCase;
import com.api.e_commerce.domain.model.Category;
import com.api.e_commerce.presentation.rest.request.CreateCategoryRequest;
import com.api.e_commerce.presentation.rest.response.CategoryResponse;
import com.api.e_commerce.presentation.rest.response.CreatedResponse;
import com.api.e_commerce.presentation.rest.response.PageResponse;
import com.api.e_commerce.presentation.rest.response.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categorias", description = "Categorias relacionais do catálogo")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final FindProductsByCategoryUseCase findProductsByCategoryUseCase;
    private final FindAllCategoriesUseCase findAllCategoriesUseCase;

    public CategoryController(CreateCategoryUseCase createCategoryUseCase,
                              FindProductsByCategoryUseCase findProductsByCategoryUseCase,
                              FindAllCategoriesUseCase findAllCategoriesUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.findProductsByCategoryUseCase = findProductsByCategoryUseCase;
        this.findAllCategoriesUseCase = findAllCategoriesUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar categoria", description = "Operação permitida para administradores ADMIN e MANAGER.",
            security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = createCategoryUseCase.execute(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(category.getId(), category.getName()));
    }

    @GetMapping
    @Operation(summary = "Listar categorias ativas")
    public ResponseEntity<List<CategoryResponse>> findAll() {
        return ResponseEntity.ok(findAllCategoriesUseCase.execute()
                .stream()
                .map(CategoryResponse::from)
                .toList());
    }

    @GetMapping("/{categoryId}/products")
    @Operation(summary = "Listar produtos ativos de uma categoria")
    public ResponseEntity<PageResponse<ProductResponse>> findProducts(
            @PathVariable java.util.UUID categoryId,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        FindProductsByCategoryUseCase.Result result =
                findProductsByCategoryUseCase.execute(categoryId, page, size);
        return ResponseEntity.ok(new PageResponse<>(
                result.products().stream().map(ProductResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }
}
