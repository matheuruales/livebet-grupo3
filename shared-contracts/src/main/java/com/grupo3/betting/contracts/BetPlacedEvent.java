package com.grupo3.betting.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record BetPlacedEvent(String eventId, String betId, String userId, String matchId,
                             String betOn, BigDecimal amount, Instant occurredAt) {
    public BetPlacedEvent {
        require(eventId, "eventId"); require(betId, "betId"); require(userId, "userId");
        require(matchId, "matchId"); require(betOn, "betOn");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount debe ser mayor que cero");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
    private static void require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " es obligatorio"); }
}
