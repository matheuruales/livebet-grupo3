package com.grupo3.betting.domain.user;

public record UserId(String value) {
    public UserId { if (value == null || value.isBlank()) throw new IllegalArgumentException("UserId es obligatorio"); }
}
