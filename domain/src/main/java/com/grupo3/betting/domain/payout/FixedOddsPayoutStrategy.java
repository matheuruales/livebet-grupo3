package com.grupo3.betting.domain.payout;

import com.grupo3.betting.domain.bet.Bet;
import com.grupo3.betting.domain.money.Money;

import java.math.BigDecimal;

public final class FixedOddsPayoutStrategy implements PayoutStrategy {
    private final BigDecimal multiplier;
    public FixedOddsPayoutStrategy(BigDecimal multiplier) {
        if (multiplier == null || multiplier.compareTo(BigDecimal.ONE) < 0) throw new IllegalArgumentException("Cuota inválida");
        this.multiplier = multiplier;
    }
    @Override public Money payout(Bet bet, String winner) {
        return bet.team().equalsIgnoreCase(winner) ? bet.stake().multiply(multiplier) : Money.ZERO;
    }
}
