package com.api.e_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "administrators",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_administrator_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_administrator_cpf", columnNames = "cpf")
        }
)
public class Administrator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    @NotBlank
    @Size(max = 150)
    private String name;

    @Column(nullable = false, length = 150)
    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String passwordHash;

    @Column(nullable = false, length = 11)
    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "CPF must contain exactly 11 digits")
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdministratorRole role = AdministratorRole.ADMIN;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Administrator() {
    }

    public Administrator(String name, String email, String passwordHash, String cpf,
                         AdministratorRole role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.cpf = cpf;
        this.role = role;
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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public AdministratorRole getRole() {
        return role;
    }

    public void setRole(AdministratorRole role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
