package com.grupo3.betting.api.account;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email"))
public class UserAccount {
    public enum Role { USER, ADMIN }

    @Id private String id;
    @Column(nullable = false, length = 80) private String name;
    @Column(nullable = false, length = 160) private String email;
    @Column(nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private Role role;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal balance;
    @Column(nullable = false) private boolean active;
    @Column(nullable = false) private Instant createdAt;

    protected UserAccount() {}

    public UserAccount(String name, String email, String passwordHash, Role role, BigDecimal balance) {
        this.id = "u-" + UUID.randomUUID().toString().substring(0, 8);
        this.name = require(name, "El nombre");
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.role = role;
        this.balance = balance;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        if (!active) throw new IllegalStateException("La cuenta está desactivada");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("El valor debe ser positivo");
        if (balance.compareTo(amount) < 0) throw new IllegalStateException("Saldo insuficiente");
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (!active) throw new IllegalStateException("La cuenta está desactivada");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("El valor debe ser positivo");
        balance = balance.add(amount);
    }

    public void setActive(boolean active) { this.active = active; }
    public void setRole(Role role) { this.role = role; }

    public String id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public String passwordHash() { return passwordHash; }
    public Role role() { return role; }
    public BigDecimal balance() { return balance; }
    public boolean active() { return active; }
    public Instant createdAt() { return createdAt; }

    public static String normalizeEmail(String value) {
        return require(value, "El correo").toLowerCase(java.util.Locale.ROOT);
    }
    private static String require(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " es obligatorio");
        return value.trim();
    }
}
