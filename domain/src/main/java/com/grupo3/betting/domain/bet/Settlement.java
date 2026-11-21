package com.grupo3.betting.domain.bet;

import com.grupo3.betting.domain.money.Money;

public record Settlement(Bet bet, boolean winner, Money payout) {}
