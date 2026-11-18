package com.grupo3.betting.domain;

final class SettledState implements MatchState { @Override public MatchStatus status() { return MatchStatus.SETTLED; } }
