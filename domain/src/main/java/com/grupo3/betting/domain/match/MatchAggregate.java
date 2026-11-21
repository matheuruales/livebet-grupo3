package com.grupo3.betting.domain.match;

import com.grupo3.betting.domain.bet.Bet;
import com.grupo3.betting.domain.bet.Settlement;
import com.grupo3.betting.domain.money.Money;
import com.grupo3.betting.domain.payout.PayoutStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MatchAggregate {
    private final MatchId id;
    private final List<String> teams;
    private final List<Bet> bets = new ArrayList<>();
    private List<Settlement> settlements = List.of();
    private String winner;
    private MatchState state;

    private MatchAggregate(MatchId id, List<String> teams) {
        this.id = Objects.requireNonNull(id);
        if (teams == null || teams.size() != 2 || teams.get(0).equalsIgnoreCase(teams.get(1)))
            throw new IllegalArgumentException("El partido requiere dos equipos diferentes");
        this.teams = List.copyOf(teams);
        this.state = new OpenForBetsState();
    }
    public static MatchAggregate create(MatchId id, List<String> teams) { return new MatchAggregate(id, teams); }
    public void placeBet(Bet bet) {
        if (!teams.stream().anyMatch(t -> t.equalsIgnoreCase(bet.team()))) throw new IllegalArgumentException("El equipo no participa en el partido");
        if (bets.stream().anyMatch(b -> b.id().equals(bet.id()))) return;
        state.placeBet(this, bet);
    }
    public void finish(String winner) {
        if (teams.stream().noneMatch(t -> t.equalsIgnoreCase(winner))) throw new IllegalArgumentException("Ganador inválido");
        state.finish(this, winner);
    }
    public void settle(PayoutStrategy strategy) { state.settle(this, strategy); }
    void addBet(Bet bet) { bets.add(Objects.requireNonNull(bet)); }
    void markClosed(String winner) { this.winner = winner; this.state = new ClosedState(); }
    void calculateSettlements(PayoutStrategy strategy) {
        settlements = bets.stream().map(b -> {
            Money payout = strategy.payout(b, winner);
            return new Settlement(b, payout.amount().signum() > 0, payout);
        }).toList();
        state = new SettledState();
    }
    public MatchId id() { return id; }
    public List<String> teams() { return teams; }
    public List<Bet> bets() { return List.copyOf(bets); }
    public List<Settlement> settlements() { return settlements; }
    public String winner() { return winner; }
    public MatchStatus status() { return state.status(); }
    public BigDecimal totalBet() { return bets.stream().map(b -> b.stake().amount()).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
