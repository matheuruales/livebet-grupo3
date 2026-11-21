package com.grupo3.betting.contracts.event;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record MatchCreatedEvent(String eventId, EventType eventType, String matchId, List<String> teams,
                                Instant occurredAt, Instant endsAt) {
    public MatchCreatedEvent(String eventId, String matchId, List<String> teams, Instant occurredAt, Instant endsAt) {
        this(eventId, EventType.MATCH_CREATED, matchId, teams, occurredAt, endsAt);
    }
    public MatchCreatedEvent {
        if (eventType != EventType.MATCH_CREATED) throw new IllegalArgumentException("Tipo de evento inválido");
        require(eventId, "eventId"); require(matchId, "matchId");
        if (teams == null || teams.size() != 2 || teams.stream().anyMatch(t -> t == null || t.isBlank()))
            throw new IllegalArgumentException("teams debe contener exactamente dos equipos");
        teams = List.copyOf(teams);
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (endsAt != null && !endsAt.isAfter(occurredAt))
            throw new IllegalArgumentException("endsAt debe ser posterior a occurredAt");
    }
    private static void require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " es obligatorio"); }
}
