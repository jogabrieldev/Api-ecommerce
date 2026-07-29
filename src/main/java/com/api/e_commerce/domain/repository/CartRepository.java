package com.api.e_commerce.domain.repository;

import com.api.e_commerce.domain.model.Cart;

import java.util.Optional;

public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findActiveByCustomerId(Long customerId);
}
