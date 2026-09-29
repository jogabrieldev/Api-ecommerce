package com.api.e_commerce.application.usecase;

import com.api.e_commerce.application.security.CustomerAccessValidator;
import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.repository.CartRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddProductToCartUseCase {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public AddProductToCartUseCase(CartRepository cartRepository,
                                   CustomerRepository customerRepository,
                                   ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart execute(java.util.UUID customerId, String authenticatedEmail, java.util.UUID productId, int quantity) {
        Customer customer = customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        CustomerAccessValidator.validateOwner(customer, authenticatedEmail);

        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Cart cart = cartRepository.findActiveByCustomerId(customerId)
                .orElseGet(() -> new Cart(customer));
        int currentQuantity = cart.findItem(productId)
                .map(item -> item.getQuantity())
                .orElse(0);
        validateStock(product, currentQuantity + quantity);

        cart.addProduct(product, quantity);
        return cartRepository.save(cart);
    }

    private void validateStock(Product product, int requestedQuantity) {
        if (requestedQuantity > product.getStock()) {
            throw new BusinessRuleException("Requested quantity exceeds available stock of " + product.getStock());
        }
    }
}
