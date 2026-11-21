package com.grupo3.betting.domain.match;

import com.grupo3.betting.domain.bet.Bet;
import com.grupo3.betting.domain.payout.PayoutStrategy;

interface MatchState {
    MatchStatus status();
    default void placeBet(MatchAggregate match, Bet bet) { throw new BettingClosedException("El partido no admite apuestas en estado " + status()); }
    default void finish(MatchAggregate match, String winner) { throw new InvalidMatchTransitionException("No se puede cerrar desde " + status()); }
    default void settle(MatchAggregate match, PayoutStrategy strategy) { throw new InvalidMatchTransitionException("No se puede liquidar desde " + status()); }
}
