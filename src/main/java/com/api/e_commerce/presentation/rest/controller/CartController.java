package com.api.e_commerce.presentation.rest.controller;

import com.api.e_commerce.application.usecase.AddProductToCartUseCase;
import com.api.e_commerce.application.usecase.ClearCartUseCase;
import com.api.e_commerce.application.usecase.CheckoutCartUseCase;
import com.api.e_commerce.application.usecase.FindActiveCartUseCase;
import com.api.e_commerce.application.usecase.RemoveCartItemUseCase;
import com.api.e_commerce.application.usecase.UpdateCartItemQuantityUseCase;
import com.api.e_commerce.presentation.rest.request.AddCartItemRequest;
import com.api.e_commerce.presentation.rest.request.UpdateCartItemRequest;
import com.api.e_commerce.presentation.rest.request.CheckoutRequest;
import com.api.e_commerce.presentation.rest.response.CartResponse;
import com.api.e_commerce.presentation.rest.response.OrderResponse;
import com.api.e_commerce.presentation.rest.response.CheckoutResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers/{customerId}/cart")
@Validated
@Tag(name = "Carrinho, pedidos e pagamentos", description = "Carrinho, estoque, checkout e pedido")
public class CartController {

    private final AddProductToCartUseCase addProductToCartUseCase;
    private final FindActiveCartUseCase findActiveCartUseCase;
    private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final CheckoutCartUseCase checkoutCartUseCase;

    public CartController(AddProductToCartUseCase addProductToCartUseCase,
                          FindActiveCartUseCase findActiveCartUseCase,
                          UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase,
                          RemoveCartItemUseCase removeCartItemUseCase,
                          ClearCartUseCase clearCartUseCase,
                          CheckoutCartUseCase checkoutCartUseCase) {
        this.addProductToCartUseCase = addProductToCartUseCase;
        this.findActiveCartUseCase = findActiveCartUseCase;
        this.updateCartItemQuantityUseCase = updateCartItemQuantityUseCase;
        this.removeCartItemUseCase = removeCartItemUseCase;
        this.clearCartUseCase = clearCartUseCase;
        this.checkoutCartUseCase = checkoutCartUseCase;
    }

    @PostMapping("/items")
    @Operation(summary = "Adicionar produto ao carrinho")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable java.util.UUID customerId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(CartResponse.from(
                addProductToCartUseCase.execute(
                        customerId, request.productId(), request.quantity())));
    }

    @GetMapping
    @Operation(summary = "Consultar carrinho ativo")
    public ResponseEntity<CartResponse> findActive(
            @PathVariable java.util.UUID customerId) {
        return ResponseEntity.ok(
                CartResponse.from(findActiveCartUseCase.execute(customerId)));
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Alterar quantidade do item")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable java.util.UUID customerId,
            @PathVariable java.util.UUID productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(CartResponse.from(
                updateCartItemQuantityUseCase.execute(
                        customerId, productId, request.quantity())));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remover item do carrinho")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable java.util.UUID customerId,
            @PathVariable java.util.UUID productId) {
        return ResponseEntity.ok(CartResponse.from(
                removeCartItemUseCase.execute(customerId, productId)));
    }

    @DeleteMapping
    @Operation(summary = "Limpar carrinho")
    public ResponseEntity<Void> clear(
            @PathVariable java.util.UUID customerId) {
        clearCartUseCase.execute(customerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    @Operation(summary = "Finalizar carrinho e criar pedido",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CheckoutResponse> checkout(
            @PathVariable java.util.UUID customerId,
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CheckoutResponse.from(
                        checkoutCartUseCase.execute(
                                customerId,
                                authentication.getName(),
                                request.paymentMethod(),
                                request.paymentToken(),
                                request.idempotencyKey())));
    }
}
