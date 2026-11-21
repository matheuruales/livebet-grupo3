package com.grupo3.betting.domain.match;

final class SettledState implements MatchState { @Override public MatchStatus status() { return MatchStatus.SETTLED; } }
