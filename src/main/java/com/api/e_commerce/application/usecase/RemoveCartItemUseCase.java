package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.repository.CartRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveCartItemUseCase {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;

    public RemoveCartItemUseCase(CartRepository cartRepository,
                                 CustomerRepository customerRepository) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Cart execute(java.util.UUID customerId, java.util.UUID productId) {
        customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Cart cart = cartRepository.findActiveByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));
        if (!cart.removeProduct(productId)) {
            throw new ResourceNotFoundException("Product is not in the cart");
        }
        return cartRepository.save(cart);
    }
}
