package com.grupo3.betting.api;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bet_event_outbox", indexes = @Index(name = "idx_outbox_pending", columnList = "publishedAt,createdAt"))
class BetOutboxEntity {
    @Id private String eventId;
    @Column(nullable = false) private String matchId;
    @Column(nullable = false, columnDefinition = "TEXT") private String payload;
    @Column(nullable = false) private Instant createdAt;
    private Instant publishedAt;
    @Column(nullable = false) private int attempts;
    @Column(length = 500) private String lastError;

    protected BetOutboxEntity() {}
    BetOutboxEntity(String eventId, String matchId, String payload) {
        this.eventId = eventId; this.matchId = matchId; this.payload = payload; this.createdAt = Instant.now();
    }
    String payload() { return payload; }
    void published() { this.publishedAt = Instant.now(); this.lastError = null; }
    void failed(Exception exception) {
        attempts++;
        String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        lastError = message.substring(0, Math.min(message.length(), 500));
    }
}
