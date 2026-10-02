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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_customer"))
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Cart() {}

    public Cart(Customer customer) {
        this.customer = customer;
    }

    public void addProduct(Product product, int quantity) {
        Optional<CartItem> existingItem = findItem(product.getId());
        if (existingItem.isPresent()) {
            existingItem.get().changeQuantityAndPrice(
                    existingItem.get().getQuantity() + quantity,
                    product.getPrice());
            touch();
            return;
        }
        items.add(new CartItem(this, product, quantity, product.getPrice()));
        touch();
    }

    public void changeProductQuantity(Product product, int quantity) {
        CartItem item = findItem(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product is not in the cart"));
        item.changeQuantityAndPrice(quantity, product.getPrice());
        touch();
    }

    public boolean removeProduct(java.util.UUID productId) {
        boolean removed = items.removeIf(item -> item.getProduct().getId().equals(productId));
        if (removed) {
            touch();
        }
        return removed;
    }

    public void clear() {
        items.clear();
        touch();
    }

    public void complete() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Empty cart cannot be completed");
        }
        status = CartStatus.COMPLETED;
        touch();
    }

    public Optional<CartItem> findItem(java.util.UUID productId) {
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private void touch() {
        updatedAt = LocalDateTime.now();
    }

    public java.util.UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public CartStatus getStatus() {
        return status;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
