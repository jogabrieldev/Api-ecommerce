package com.api.e_commerce.application.usecase;

import com.api.e_commerce.application.security.CustomerAccessValidator;
import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.repository.CartRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClearCartUseCase {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;

    public ClearCartUseCase(CartRepository cartRepository,
                            CustomerRepository customerRepository) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void execute(java.util.UUID customerId, String authenticatedEmail) {
        var customer = customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        CustomerAccessValidator.validateOwner(customer, authenticatedEmail);
        Cart cart = cartRepository.findActiveByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));
        cart.clear();
        cartRepository.save(cart);
    }
}
