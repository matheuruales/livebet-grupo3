package com.grupo3.betting.api.recharge;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_recharges", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_recharge_order", columnNames = "provider_order_id"),
        @UniqueConstraint(name = "uk_payment_recharge_idempotency", columnNames = "idempotency_key")
})
class PaymentRecharge {
    enum Status { CREATING, PENDING, APPROVED, REJECTED, ERROR }

    @Id @Column(length = 64) private String reference;
    @Column(name = "user_id", nullable = false, length = 16) private String userId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(name = "idempotency_key", nullable = false, length = 36) private String idempotencyKey;
    @Column(name = "provider_order_id", length = 80) private String providerOrderId;
    @Column(name = "checkout_url", length = 1000) private String checkoutUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private Status status;
    @Column(nullable = false) private Instant createdAt;
    private Instant processedAt;
    @Version private long version;

    protected PaymentRecharge() {}

    PaymentRecharge(String userId, BigDecimal amount) {
        this.reference = "rcg-" + UUID.randomUUID();
        this.userId = userId;
        this.amount = amount.setScale(2);
        this.idempotencyKey = UUID.randomUUID().toString();
        this.status = Status.CREATING;
        this.createdAt = Instant.now();
    }

    void attach(PaymentOrder order) {
        if (!reference.equals(order.externalReference())) {
            throw new IllegalStateException("La referencia devuelta por Mercado Pago no coincide");
        }
        bindOrder(order.id());
        if (order.checkoutUrl() == null || !order.checkoutUrl().startsWith("https://")) {
            throw new PaymentGatewayUnavailableException("Mercado Pago no devolvió un enlace de pago válido");
        }
        checkoutUrl = order.checkoutUrl();
        status = Status.PENDING;
    }

    void bindOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("Orden de Mercado Pago inválida");
        if (providerOrderId != null && !providerOrderId.equals(orderId)) {
            throw new IllegalArgumentException("La orden de Mercado Pago no coincide con la recarga");
        }
        providerOrderId = orderId;
    }

    void approve() {
        if (status == Status.APPROVED) return;
        status = Status.APPROVED;
        processedAt = Instant.now();
    }

    void reject() {
        if (status == Status.APPROVED) return;
        status = Status.REJECTED;
        processedAt = Instant.now();
    }

    void error() { if (status != Status.APPROVED) status = Status.ERROR; }

    String reference() { return reference; }
    String userId() { return userId; }
    BigDecimal amount() { return amount; }
    String idempotencyKey() { return idempotencyKey; }
    String providerOrderId() { return providerOrderId; }
    String checkoutUrl() { return checkoutUrl; }
    Status status() { return status; }
    Instant createdAt() { return createdAt; }
    Instant processedAt() { return processedAt; }
}
