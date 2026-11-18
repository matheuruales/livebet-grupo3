package com.grupo3.betting.api;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bet_requests", indexes = @Index(name = "idx_bet_request_user", columnList = "userId"))
class BetRequestEntity {
    @Id private String id;
    @Column(nullable = false) private String userId;
    @Column(nullable = false) private String matchId;
    @Column(nullable = false) private String betOn;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private Instant createdAt;

    protected BetRequestEntity() {}
    BetRequestEntity(String id, String userId, String matchId, String betOn, BigDecimal amount) {
        this.id = id; this.userId = userId; this.matchId = matchId; this.betOn = betOn; this.amount = amount;
        this.status = "ACCEPTED_FOR_PROCESSING"; this.createdAt = Instant.now();
    }
    BetView view() { return new BetView(id, matchId, betOn, amount, status, createdAt); }
    record BetView(String betId, String matchId, String betOn, BigDecimal amount, String status, Instant createdAt) {}
}
