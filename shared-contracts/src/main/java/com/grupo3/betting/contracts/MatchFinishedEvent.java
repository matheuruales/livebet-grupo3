package com.grupo3.betting.contracts;

import java.time.Instant;
import java.util.Objects;

public record MatchFinishedEvent(String eventId, String matchId, String winner, Instant occurredAt) {
    public MatchFinishedEvent {
        require(eventId, "eventId"); require(matchId, "matchId"); require(winner, "winner");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
    private static void require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " es obligatorio"); }
}
