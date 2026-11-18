package com.grupo3.betting.domain;

import java.util.Objects;

public record Bet(BetId id, UserId userId, String team, Money stake) {
    public Bet {
        Objects.requireNonNull(id); Objects.requireNonNull(userId); Objects.requireNonNull(stake);
        if (team == null || team.isBlank()) throw new IllegalArgumentException("El equipo es obligatorio");
        if (stake.amount().signum() <= 0) throw new IllegalArgumentException("La apuesta debe ser mayor que cero");
    }
}
