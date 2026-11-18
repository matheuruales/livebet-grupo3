package com.grupo3.betting.engine;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="matches")
class MatchAuditEntity {
    @Id String id;
    String homeTeam; String awayTeam; String status; String winner;
    BigDecimal totalBet; Instant closedAt;
    @OneToMany(mappedBy="match", cascade=CascadeType.ALL, orphanRemoval=true) List<BetAuditEntity> bets = new ArrayList<>();
    protected MatchAuditEntity() {}
    MatchAuditEntity(String id, String homeTeam, String awayTeam, String status, String winner, BigDecimal totalBet, Instant closedAt) {
        this.id=id; this.homeTeam=homeTeam; this.awayTeam=awayTeam; this.status=status; this.winner=winner; this.totalBet=totalBet; this.closedAt=closedAt;
    }
    void add(BetAuditEntity bet) { bets.add(bet); bet.match = this; }
}
