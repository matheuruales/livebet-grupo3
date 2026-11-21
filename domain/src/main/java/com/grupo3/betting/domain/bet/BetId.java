package com.grupo3.betting.domain.bet;

public record BetId(String value) {
    public BetId { if (value == null || value.isBlank()) throw new IllegalArgumentException("BetId es obligatorio"); }
}
