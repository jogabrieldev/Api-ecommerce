package com.api.e_commerce.controller;

import com.api.e_commerce.model.Category;
import com.api.e_commerce.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CreatedResponse> create(@Valid @RequestBody CreateRequest request) {
        Category category = categoryService.create(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResponse(category.getId()));
    }

    public record CreateRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description
    ) {
    }

    public record CreatedResponse(Long id) {
    }
}
