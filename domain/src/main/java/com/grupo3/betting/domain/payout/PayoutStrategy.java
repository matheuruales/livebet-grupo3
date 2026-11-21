package com.grupo3.betting.domain.payout;

import com.grupo3.betting.domain.bet.Bet;
import com.grupo3.betting.domain.money.Money;

public interface PayoutStrategy { Money payout(Bet bet, String winner); }
