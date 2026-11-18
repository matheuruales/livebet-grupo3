package com.grupo3.betting.contracts;

import java.time.Instant;
import java.util.Objects;

public record MatchFinishedEvent(String eventId, EventType eventType, String matchId, String winner,
                                 Integer homeScore, Integer awayScore, Instant occurredAt) {
    public MatchFinishedEvent(String eventId, String matchId, String winner,
                              Integer homeScore, Integer awayScore, Instant occurredAt) {
        this(eventId, EventType.MATCH_FINISHED, matchId, winner, homeScore, awayScore, occurredAt);
    }
    public MatchFinishedEvent {
        if (eventType != EventType.MATCH_FINISHED) throw new IllegalArgumentException("Tipo de evento inválido");
        require(eventId, "eventId"); require(matchId, "matchId"); require(winner, "winner");
        if ((homeScore == null) != (awayScore == null))
            throw new IllegalArgumentException("El marcador debe incluir ambos resultados");
        if (homeScore != null && (homeScore < 0 || awayScore < 0 || homeScore.equals(awayScore)))
            throw new IllegalArgumentException("El marcador debe ser no negativo y tener un ganador");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
    private static void require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " es obligatorio"); }
}
