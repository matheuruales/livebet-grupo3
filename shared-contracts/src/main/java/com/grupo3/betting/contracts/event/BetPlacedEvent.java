package com.grupo3.betting.contracts.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record BetPlacedEvent(String eventId, EventType eventType, String betId, String userId, String userEmail,
                             String matchId, String betOn, BigDecimal amount, Instant occurredAt) {
    public BetPlacedEvent(String eventId, String betId, String userId, String matchId,
                          String betOn, BigDecimal amount, Instant occurredAt) {
        this(eventId, EventType.BET_PLACED, betId, userId, null, matchId, betOn, amount, occurredAt);
    }
    public BetPlacedEvent(String eventId, String betId, String userId, String userEmail, String matchId,
                          String betOn, BigDecimal amount, Instant occurredAt) {
        this(eventId, EventType.BET_PLACED, betId, userId, userEmail, matchId, betOn, amount, occurredAt);
    }
    public BetPlacedEvent {
        if (eventType != EventType.BET_PLACED) throw new IllegalArgumentException("Tipo de evento inválido");
        require(eventId, "eventId"); require(betId, "betId"); require(userId, "userId");
        require(matchId, "matchId"); require(betOn, "betOn");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount debe ser mayor que cero");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
    private static void require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " es obligatorio"); }
}
