package com.api.e_commerce.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_allocations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payment_allocation_administrator",
                columnNames = {"payment_id", "administrator_id"}))
public class PaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_allocation_payment"))
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrator_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_allocation_administrator"))
    private Administrator administrator;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected PaymentAllocation() {
    }

    PaymentAllocation(Payment payment, Administrator administrator, BigDecimal amount) {
        this.payment = payment;
        this.administrator = administrator;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Administrator getAdministrator() {
        return administrator;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
