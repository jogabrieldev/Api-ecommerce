package com.api.e_commerce.application.usecase;

import com.api.e_commerce.application.security.CustomerAccessValidator;
import com.api.e_commerce.domain.exception.BusinessRuleException;
import com.api.e_commerce.domain.exception.ResourceNotFoundException;
import com.api.e_commerce.domain.exception.ConflictException;
import com.api.e_commerce.domain.exception.PaymentDeclinedException;
import com.api.e_commerce.domain.model.Cart;
import com.api.e_commerce.domain.model.CartItem;
import com.api.e_commerce.domain.model.Customer;
import com.api.e_commerce.domain.model.Order;
import com.api.e_commerce.domain.model.Product;
import com.api.e_commerce.domain.model.Payment;
import com.api.e_commerce.domain.model.PaymentAllocation;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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
    public Result execute(java.util.UUID customerId, String authenticatedEmail,
                          PaymentMethod paymentMethod, String paymentToken,
                          String idempotencyKey) {
        Customer customer = customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        CustomerAccessValidator.validateOwner(customer, authenticatedEmail);
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
        PaymentGateway.Result charge;
        try {
            charge = paymentGateway.charge(paymentToken, paymentMethod, payment.getAmount(), payment.getCurrency());
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException(exception.getMessage());
        }
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
        try {
            payment.approve(order, charge.reference());
        } catch (IllegalStateException exception) {
            throw new ConflictException(exception.getMessage());
        }
        paymentRepository.save(payment);
        return Result.from(order, payment, false);
    }

    private Result handleIdempotentRetry(Payment payment, java.util.UUID customerId) {
        if (!payment.getCustomer().getId().equals(customerId)) {
            throw new ConflictException("Idempotency key belongs to another customer");
        }
        if (payment.getStatus() == PaymentStatus.APPROVED && payment.getOrder() != null) {
            return Result.from(payment.getOrder(), payment, true);
        }
        if (payment.getStatus() == PaymentStatus.DECLINED) {
            throw new PaymentDeclinedException(payment);
        }
        throw new ConflictException("Payment with this idempotency key is still processing");
    }

    private void validateAndDecreaseStock(CartItem item) {
        java.util.UUID productId = item.getProduct().getId();
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

    public record Result(OrderData order, PaymentData payment, boolean replayed) {

        private static Result from(Order order, Payment payment, boolean replayed) {
            return new Result(OrderData.from(order), PaymentData.from(payment), replayed);
        }
    }

    public record OrderData(
            java.util.UUID id,
            java.util.UUID customerId,
            java.util.UUID cartId,
            com.api.e_commerce.domain.model.OrderStatus status,
            List<OrderItemData> items,
            BigDecimal total,
            LocalDateTime createdAt
    ) {
        private static OrderData from(Order order) {
            return new OrderData(
                    order.getId(),
                    order.getCustomer().getId(),
                    order.getCart().getId(),
                    order.getStatus(),
                    order.getItems().stream().map(OrderItemData::from).toList(),
                    order.getTotal(),
                    order.getCreatedAt()
            );
        }
    }

    public record OrderItemData(
            java.util.UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
        private static OrderItemData from(com.api.e_commerce.domain.model.OrderItem item) {
            return new OrderItemData(
                    item.getProduct().getId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()
            );
        }
    }

    public record PaymentData(
            java.util.UUID id,
            PaymentStatus status,
            PaymentMethod method,
            BigDecimal amount,
            String currency,
            String gatewayReference,
            String idempotencyKey,
            List<AllocationData> allocations,
            LocalDateTime createdAt
    ) {
        private static PaymentData from(Payment payment) {
            return new PaymentData(
                    payment.getId(),
                    payment.getStatus(),
                    payment.getMethod(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getGatewayReference(),
                    payment.getIdempotencyKey(),
                    payment.getAllocations().stream().map(AllocationData::from).toList(),
                    payment.getCreatedAt()
            );
        }
    }

    public record AllocationData(
            java.util.UUID administratorId,
            String administratorName,
            BigDecimal amount
    ) {
        private static AllocationData from(PaymentAllocation allocation) {
            return new AllocationData(
                    allocation.getAdministrator().getId(),
                    allocation.getAdministrator().getName(),
                    allocation.getAmount()
            );
        }
    }
}
