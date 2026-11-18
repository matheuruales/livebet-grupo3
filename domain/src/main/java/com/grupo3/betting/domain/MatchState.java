package com.grupo3.betting.domain;

interface MatchState {
    MatchStatus status();
    default void placeBet(MatchAggregate match, Bet bet) { throw new BettingClosedException("El partido no admite apuestas en estado " + status()); }
    default void finish(MatchAggregate match, String winner) { throw new InvalidMatchTransitionException("No se puede cerrar desde " + status()); }
    default void settle(MatchAggregate match, PayoutStrategy strategy) { throw new InvalidMatchTransitionException("No se puede liquidar desde " + status()); }
}
