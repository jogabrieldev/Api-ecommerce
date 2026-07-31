package com.api.e_commerce.application.usecase;

import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ForbiddenOperationException;
import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.exception.ConflictException;
import com.api.e_commerce.domain.exception.PaymentDeclinedException;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.CartItem;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.Order;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.model.Payment;
import com.api.e_commerce.domain.model.PaymentMethod;
import com.api.e_commerce.domain.model.PaymentStatus;
import com.api.e_commerce.domain.payment.PaymentGateway;
import com.api.e_commerce.domain.repository.CartRepository;
import com.api.e_commerce.domain.repository.CustomerRepository;
import com.api.e_commerce.domain.repository.OrderRepository;
import com.api.e_commerce.domain.repository.ProductRepository;
import com.api.e_commerce.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
public class CheckoutCartUseCase {

    private final CustomerRepository customerRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    public CheckoutCartUseCase(CustomerRepository customerRepository,
                               CartRepository cartRepository,
                               ProductRepository productRepository,
                               OrderRepository orderRepository,
                               PaymentRepository paymentRepository,
                               PaymentGateway paymentGateway) {
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional(noRollbackFor = PaymentDeclinedException.class)
    public Result execute(Long customerId, String authenticatedEmail,
                          PaymentMethod paymentMethod, String paymentToken,
                          String idempotencyKey) {
        Customer customer = customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        validateAuthenticatedCustomer(customer, authenticatedEmail);
        Payment existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey)
                .orElse(null);
        if (existingPayment != null) {
            return handleIdempotentRetry(existingPayment, customerId);
        }

        Cart cart = cartRepository.findActiveByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Cart is empty");
        }

        Payment payment = new Payment(customer, cart, paymentMethod, idempotencyKey);
        PaymentGateway.Result charge = paymentGateway.charge(
                paymentToken, paymentMethod, payment.getAmount(), payment.getCurrency());
        if (!charge.approved()) {
            payment.decline(charge.declineReason(), charge.reference());
            paymentRepository.save(payment);
            throw new PaymentDeclinedException(payment);
        }

        cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .forEach(this::validateAndDecreaseStock);

        Order order = new Order(customer, cart);
        cart.complete();
        orderRepository.save(order);
        payment.approve(order, charge.reference());
        paymentRepository.save(payment);
        return new Result(order, payment);
    }

    private Result handleIdempotentRetry(Payment payment, Long customerId) {
        if (!payment.getCustomer().getId().equals(customerId)) {
            throw new ConflictException("Idempotency key belongs to another customer");
        }
        if (payment.getStatus() == PaymentStatus.APPROVED && payment.getOrder() != null) {
            return new Result(payment.getOrder(), payment);
        }
        if (payment.getStatus() == PaymentStatus.DECLINED) {
            throw new PaymentDeclinedException(payment);
        }
        throw new ConflictException("Payment with this idempotency key is still processing");
    }

    private void validateAuthenticatedCustomer(Customer customer, String authenticatedEmail) {
        if (!Boolean.TRUE.equals(customer.getActive())) {
            throw new BusinessRuleException("Inactive customer cannot complete checkout");
        }
        if (!customer.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new ForbiddenOperationException(
                    "Authenticated customer cannot complete another customer's cart");
        }
    }

    private void validateAndDecreaseStock(CartItem item) {
        Long productId = item.getProduct().getId();
        Product product = productRepository.findActiveByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Product " + productId + " is unavailable"));

        if (item.getQuantity() > product.getStock()) {
            throw new BusinessRuleException(
                    "Insufficient stock for product " + product.getName()
                            + ". Available: " + product.getStock()
                            + ", requested: " + item.getQuantity());
        }
        product.decreaseStock(item.getQuantity());
    }

    public record Result(Order order, Payment payment) {
    }
}
