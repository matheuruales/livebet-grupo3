package com.grupo3.betting.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class MatchAggregateTest {
    private MatchAggregate match() { return MatchAggregate.create(new MatchId("m-1"), List.of("Cúcuta", "Pasto")); }
    private Bet bet(String id, String team, int amount) { return new Bet(new BetId(id), new UserId("u-1"), team, new Money(BigDecimal.valueOf(amount))); }

    @Test void acceptsBetWhileOpen() {
        var match = match(); match.placeBet(bet("b-1", "Cúcuta", 100));
        assertThat(match.bets()).hasSize(1); assertThat(match.totalBet()).isEqualByComparingTo("100.00");
    }
    @Test void rejectsLateBet() {
        var match = match(); match.finish("Pasto");
        assertThatThrownBy(() -> match.placeBet(bet("b-1", "Pasto", 50))).isInstanceOf(BettingClosedException.class);
    }
    @Test void settlesWinnerWithFixedOdds() {
        var match = match(); match.placeBet(bet("b-1", "Cúcuta", 100)); match.finish("Cúcuta");
        match.settle(new FixedOddsPayoutStrategy(BigDecimal.valueOf(2)));
        assertThat(match.status()).isEqualTo(MatchStatus.SETTLED);
        assertThat(match.settlements().get(0).payout().amount()).isEqualByComparingTo("200.00");
    }
    @Test void moneyFailsFast() {
        assertThatThrownBy(() -> new Money(BigDecimal.valueOf(-1))).isInstanceOf(IllegalArgumentException.class);
    }
}
