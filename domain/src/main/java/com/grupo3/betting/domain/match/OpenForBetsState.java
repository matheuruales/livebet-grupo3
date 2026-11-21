package com.grupo3.betting.domain.match;

import com.grupo3.betting.domain.bet.Bet;

final class OpenForBetsState implements MatchState {
    @Override public MatchStatus status() { return MatchStatus.OPEN_FOR_BETS; }
    @Override public void placeBet(MatchAggregate match, Bet bet) { match.addBet(bet); }
    @Override public void finish(MatchAggregate match, String winner) { match.markClosed(winner); }
}
