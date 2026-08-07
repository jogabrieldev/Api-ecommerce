package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.CreateProductUseCase;
import com.api.e_commerce.application.usecase.FindAllProductsUseCase;
import com.api.e_commerce.application.usecase.FindProductByIdUseCase;
import com.api.e_commerce.application.usecase.SearchProductsUseCase;
import com.api.e_commerce.application.usecase.ImportFakeStoreProductsUseCase;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.presentation.rest.request.CreateProductRequest;
import com.api.e_commerce.presentation.rest.response.CreatedResponse;
import com.api.e_commerce.presentation.rest.response.ProductResponse;
import com.api.e_commerce.presentation.rest.response.PageResponse;
import com.api.e_commerce.presentation.rest.response.ProductImportResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/products")
@Validated
@Tag(name = "Produtos", description = "Catálogo, pesquisa, estoque e importação")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final FindAllProductsUseCase findAllProductsUseCase;
    private final FindProductByIdUseCase findProductByIdUseCase;
    private final SearchProductsUseCase searchProductsUseCase;
    private final ImportFakeStoreProductsUseCase importFakeStoreProductsUseCase;

    public ProductController(CreateProductUseCase createProductUseCase,
                             FindAllProductsUseCase findAllProductsUseCase,
                             FindProductByIdUseCase findProductByIdUseCase,
                             SearchProductsUseCase searchProductsUseCase,
                             ImportFakeStoreProductsUseCase importFakeStoreProductsUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.findAllProductsUseCase = findAllProductsUseCase;
        this.findProductByIdUseCase = findProductByIdUseCase;
        this.searchProductsUseCase = searchProductsUseCase;
        this.importFakeStoreProductsUseCase = importFakeStoreProductsUseCase;
    }

    @PostMapping("/import/fake-store")
    @Operation(summary = "Importar produtos da Fake Store",
            description = "Normaliza e persiste produtos por fonte e ID externo, sem duplicação.",
            security = @SecurityRequirement(name = "basicAuth"))
    public ResponseEntity<ProductImportResponse> importFakeStore(Authentication authentication) {
        return ResponseEntity.ok(ProductImportResponse.from(
                importFakeStoreProductsUseCase.execute(authentication.getName())));
    }

    @PostMapping
    @Operation(summary = "Cadastrar produto", security = @SecurityRequirement(name = "basicAuth"))
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
    @Operation(summary = "Listar produtos")
    public ResponseEntity<List<ProductResponse>> findAll() {
        List<ProductResponse> products = findAllProductsUseCase.execute()
                .stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar produto por ID")
    public ResponseEntity<ProductResponse> findById(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(ProductResponse.from(findProductByIdUseCase.execute(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Pesquisar produtos ativos")
    public ResponseEntity<PageResponse<ProductResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) java.util.UUID categoryId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        SearchProductsUseCase.Result result =
                searchProductsUseCase.execute(name, categoryId, page, size);
        List<ProductResponse> content = result.products()
                .stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(new PageResponse<>(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        ));
    }
}
