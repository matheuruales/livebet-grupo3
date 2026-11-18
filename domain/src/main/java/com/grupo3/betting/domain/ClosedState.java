package com.grupo3.betting.domain;

final class ClosedState implements MatchState {
    @Override public MatchStatus status() { return MatchStatus.CLOSED; }
    @Override public void settle(MatchAggregate match, PayoutStrategy strategy) { match.calculateSettlements(strategy); }
}
