package com.grupo3.betting.api;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email"))
class UserAccount {
    enum Role { USER, ADMIN }

    @Id private String id;
    @Column(nullable = false, length = 80) private String name;
    @Column(nullable = false, length = 160) private String email;
    @Column(nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private Role role;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal balance;
    @Column(nullable = false) private boolean active;
    @Column(nullable = false) private Instant createdAt;

    protected UserAccount() {}

    UserAccount(String name, String email, String passwordHash, Role role, BigDecimal balance) {
        this.id = "u-" + UUID.randomUUID().toString().substring(0, 8);
        this.name = require(name, "El nombre");
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.role = role;
        this.balance = balance;
        this.active = true;
        this.createdAt = Instant.now();
    }

    void debit(BigDecimal amount) {
        if (!active) throw new IllegalStateException("La cuenta está desactivada");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("El valor debe ser positivo");
        if (balance.compareTo(amount) < 0) throw new IllegalStateException("Saldo insuficiente");
        balance = balance.subtract(amount);
    }

    void credit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("El valor debe ser positivo");
        balance = balance.add(amount);
    }

    void setActive(boolean active) { this.active = active; }
    void setRole(Role role) { this.role = role; }

    String id() { return id; }
    String name() { return name; }
    String email() { return email; }
    String passwordHash() { return passwordHash; }
    Role role() { return role; }
    BigDecimal balance() { return balance; }
    boolean active() { return active; }
    Instant createdAt() { return createdAt; }

    static String normalizeEmail(String value) {
        return require(value, "El correo").toLowerCase(java.util.Locale.ROOT);
    }
    private static String require(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " es obligatorio");
        return value.trim();
    }
}
