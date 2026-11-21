package com.grupo3.betting.api.bet;

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
    @Column(precision = 19, scale = 2) private BigDecimal payout;
    private Instant settledAt;

    protected BetRequestEntity() {}
    BetRequestEntity(String id, String userId, String matchId, String betOn, BigDecimal amount) {
        this.id = id; this.userId = userId; this.matchId = matchId; this.betOn = betOn; this.amount = amount;
        this.status = "ACCEPTED_FOR_PROCESSING"; this.createdAt = Instant.now();
    }
    boolean pending() { return "ACCEPTED_FOR_PROCESSING".equals(status); }
    String userId() { return userId; }
    String matchId() { return matchId; }
    BigDecimal amount() { return amount; }
    void settle(boolean winner, BigDecimal payout, Instant at) {
        if (!pending()) return;
        this.status = winner ? "WON" : "LOST";
        this.payout = winner ? payout : BigDecimal.ZERO;
        this.settledAt = at;
    }
    void reject(Instant at) {
        if (!pending()) return;
        this.status = "REJECTED";
        this.payout = BigDecimal.ZERO;
        this.settledAt = at;
    }
    BetView view() { return new BetView(id, matchId, betOn, amount, status, payout, createdAt, settledAt); }
    record BetView(String betId, String matchId, String betOn, BigDecimal amount, String status,
                   BigDecimal payout, Instant createdAt, Instant settledAt) {}
}
