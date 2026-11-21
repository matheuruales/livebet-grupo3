package com.grupo3.betting.api.roulette;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roulette_bets", indexes = {
        @Index(name = "idx_roulette_bet_round", columnList = "roundId"),
        @Index(name = "idx_roulette_bet_user", columnList = "userId")
})
class RouletteBet {
    enum Status { PENDING, WON, LOST }

    @Id private String id;
    @Column(nullable = false, length = 16) private String roundId;
    @Column(nullable = false, length = 16) private String userId;
    @Column(nullable = false, length = 16) private String selectionType;
    @Column(nullable = false, length = 16) private String selectionValue;
    @Column(nullable = false, length = 40) private String selectionLabel;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal payout;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private Status status;
    @Column(nullable = false) private Instant createdAt;

    protected RouletteBet() {}

    RouletteBet(String roundId, String userId, String type, String value, String label, BigDecimal amount) {
        this.id = "rb-" + UUID.randomUUID().toString().substring(0, 8);
        this.roundId = roundId;
        this.userId = userId;
        this.selectionType = type;
        this.selectionValue = value;
        this.selectionLabel = label;
        this.amount = amount;
        this.payout = BigDecimal.ZERO;
        this.status = Status.PENDING;
        this.createdAt = Instant.now();
    }

    void settle(boolean won, BigDecimal payout) { this.status = won ? Status.WON : Status.LOST; this.payout = payout; }
    String id() { return id; }
    String roundId() { return roundId; }
    String userId() { return userId; }
    String selectionType() { return selectionType; }
    String selectionValue() { return selectionValue; }
    String selectionLabel() { return selectionLabel; }
    BigDecimal amount() { return amount; }
    BigDecimal payout() { return payout; }
    Status status() { return status; }
    Instant createdAt() { return createdAt; }
}
