package com.api.e_commerce.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", uniqueConstraints = @UniqueConstraint(
        name = "uk_product_source_external_id", columnNames = {"source", "external_id"}))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(nullable = false, length = 150)
    @NotBlank
    @Size(max = 150)
    private String name;

    @Column(length = 2000)
    @Size(max = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Integer stock;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductSource source = ProductSource.INTERNAL;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_administrator_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_created_by_administrator")
    )
    @NotNull
    private Administrator createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_category"))
    @NotNull
    private Category category;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Product() {
    }

    public Product(String name, String description, BigDecimal price, Integer stock,
                   Administrator createdBy, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.createdBy = createdBy;
        this.category = category;
    }

    public Product(String name, String description, BigDecimal price, Integer stock,
                   Administrator createdBy, Category category, String externalId,
                   ProductSource source, String imageUrl) {
        this(name, description, price, stock, createdBy, category);
        this.externalId = externalId;
        this.source = source;
        this.imageUrl = imageUrl;
    }

    public void updateExternalData(String name, String description, BigDecimal price,
                                   Integer stock, Category category, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
        this.active = true;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (quantity > stock) {
            throw new IllegalStateException("Insufficient stock");
        }
        stock -= quantity;
    }

    public Boolean getActive() {
        return active;
    }

    public String getExternalId() {
        return externalId;
    }

    public ProductSource getSource() {
        return source;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Administrator getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Administrator createdBy) {
        this.createdBy = createdBy;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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
