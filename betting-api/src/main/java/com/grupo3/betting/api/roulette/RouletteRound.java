package com.grupo3.betting.api.roulette;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roulette_rounds")
class RouletteRound {
    enum Status { BETTING, SPINNING, SETTLED }

    @Id private String id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private Status status;
    @Column(nullable = false) private Instant openedAt;
    @Column(nullable = false) private Instant closesAt;
    @Column(nullable = false) private Instant resolvesAt;
    @Column(nullable = false) private Instant nextRoundAt;
    private Integer resultNumber;

    protected RouletteRound() {}

    RouletteRound(Instant now) {
        this.id = "r-" + UUID.randomUUID().toString().substring(0, 8);
        this.status = Status.BETTING;
        this.openedAt = now;
        this.closesAt = now.plusSeconds(30);
        this.resolvesAt = closesAt.plusSeconds(8);
        this.nextRoundAt = resolvesAt.plusSeconds(6);
    }

    void startSpin(int resultNumber) { this.status = Status.SPINNING; this.resultNumber = resultNumber; }
    void settle() { this.status = Status.SETTLED; }

    String id() { return id; }
    Status status() { return status; }
    Instant openedAt() { return openedAt; }
    Instant closesAt() { return closesAt; }
    Instant resolvesAt() { return resolvesAt; }
    Instant nextRoundAt() { return nextRoundAt; }
    Integer resultNumber() { return resultNumber; }
}
