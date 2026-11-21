package com.grupo3.betting.api.recharge;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recharge_transactions")
class RechargeTransaction {
    @Id private String id;
    @Column(nullable = false, length = 16) private String userId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 80) private String cardholder;
    @Column(nullable = false, length = 4) private String cardLast4;
    @Column(nullable = false, length = 16) private String status;
    @Column(nullable = false) private Instant processedAt;

    protected RechargeTransaction() {}

    RechargeTransaction(String userId, BigDecimal amount, String cardholder, String cardLast4) {
        this.id = "r-" + UUID.randomUUID().toString().substring(0, 8);
        this.userId = userId;
        this.amount = amount;
        this.cardholder = cardholder;
        this.cardLast4 = cardLast4;
        this.status = "APPROVED";
        this.processedAt = Instant.now();
    }

    RechargeView view(BigDecimal balance) {
        return new RechargeView(id, amount, cardLast4, status, processedAt, balance);
    }
}
