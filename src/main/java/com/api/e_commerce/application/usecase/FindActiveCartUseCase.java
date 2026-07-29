package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FindActiveCartUseCase {

    private final CartRepository cartRepository;

    public FindActiveCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Transactional(readOnly = true)
    public Cart execute(Long customerId) {
        return cartRepository.findActiveByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));
    }
}
