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
    public ResponseEntity<CartResponse> addItem(
            @PathVariable @Min(1) Long customerId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(CartResponse.from(
                addProductToCartUseCase.execute(
                        customerId, request.productId(), request.quantity())));
    }

    @GetMapping
    public ResponseEntity<CartResponse> findActive(
            @PathVariable @Min(1) Long customerId) {
        return ResponseEntity.ok(
                CartResponse.from(findActiveCartUseCase.execute(customerId)));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable @Min(1) Long customerId,
            @PathVariable @Min(1) Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(CartResponse.from(
                updateCartItemQuantityUseCase.execute(
                        customerId, productId, request.quantity())));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable @Min(1) Long customerId,
            @PathVariable @Min(1) Long productId) {
        return ResponseEntity.ok(CartResponse.from(
                removeCartItemUseCase.execute(customerId, productId)));
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(
            @PathVariable @Min(1) Long customerId) {
        clearCartUseCase.execute(customerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @PathVariable @Min(1) Long customerId,
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
