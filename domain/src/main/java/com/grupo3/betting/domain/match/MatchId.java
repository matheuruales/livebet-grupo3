package com.grupo3.betting.domain.match;

public record MatchId(String value) {
    public MatchId { if (value == null || value.isBlank()) throw new IllegalArgumentException("MatchId es obligatorio"); }
}
