package com.grupo3.betting.domain;

public record Settlement(Bet bet, boolean winner, Money payout) {}
