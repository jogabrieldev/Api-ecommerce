package com.api.e_commerce.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_idempotency_key",
                columnNames = "idempotency_key"))
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_cart"))
    private Cart cart;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true,
            foreignKey = @ForeignKey(name = "fk_payment_order"))
    private Order order;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "BRL";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PROCESSING;

    @Column(name = "idempotency_key", nullable = false, length = 36)
    private String idempotencyKey;

    @Column(name = "gateway_reference", length = 80)
    private String gatewayReference;

    @Column(name = "decline_reason", length = 255)
    private String declineReason;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentAllocation> allocations = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Payment() {
    }

    public Payment(Customer customer, Cart cart, PaymentMethod method, String idempotencyKey) {
        this.customer = customer;
        this.cart = cart;
        this.amount = cart.getTotal();
        this.method = method;
        this.idempotencyKey = idempotencyKey;
    }

    public void approve(Order order, String gatewayReference) {
        if (status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Only processing payments can be approved");
        }
        if (order.getTotal().compareTo(amount) != 0) {
            throw new IllegalStateException("Payment and order totals do not match");
        }
        this.order = order;
        this.gatewayReference = gatewayReference;
        this.status = PaymentStatus.APPROVED;
        createAllocations();
    }

    public void decline(String reason, String gatewayReference) {
        if (status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Only processing payments can be declined");
        }
        this.declineReason = reason;
        this.gatewayReference = gatewayReference;
        this.status = PaymentStatus.DECLINED;
    }

    private void createAllocations() {
        Map<Administrator, BigDecimal> amountsByAdministrator = new LinkedHashMap<>();
        order.getItems().forEach(item -> amountsByAdministrator.merge(
                item.getProduct().getCreatedBy(),
                item.getSubtotal(),
                BigDecimal::add
        ));
        amountsByAdministrator.forEach((administrator, value) ->
                allocations.add(new PaymentAllocation(this, administrator, value)));

        BigDecimal allocatedTotal = allocations.stream()
                .map(PaymentAllocation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (allocatedTotal.compareTo(amount) != 0) {
            throw new IllegalStateException("Payment allocations do not match payment amount");
        }
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public java.util.UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Cart getCart() {
        return cart;
    }

    public Order getOrder() {
        return order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public List<PaymentAllocation> getAllocations() {
        return Collections.unmodifiableList(allocations);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
