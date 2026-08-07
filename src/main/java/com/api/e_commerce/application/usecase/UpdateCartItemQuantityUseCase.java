package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.CartRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCartItemQuantityUseCase {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public UpdateCartItemQuantityUseCase(CartRepository cartRepository,
                                         CustomerRepository customerRepository,
                                         ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart execute(java.util.UUID customerId, java.util.UUID productId, int quantity) {
        customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Cart cart = cartRepository.findActiveByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));

        if (cart.findItem(productId).isEmpty()) {
            throw new ResourceNotFoundException("Product is not in the cart");
        }
        if (quantity > product.getStock()) {
            throw new BusinessRuleException(
                    "Requested quantity exceeds available stock of " + product.getStock());
        }

        cart.changeProductQuantity(product, quantity);
        return cartRepository.save(cart);
    }
}
