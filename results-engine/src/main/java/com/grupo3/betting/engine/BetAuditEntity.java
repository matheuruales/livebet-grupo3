package com.grupo3.betting.engine;

import com.grupo3.betting.contracts.BetPlacedEvent;
import com.grupo3.betting.domain.Bet;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="bets")
class BetAuditEntity {
    @Id String id; String userId; String betOn; BigDecimal stake; boolean winner; BigDecimal payout;
    String status;
    String rejectionReason;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="match_id") MatchAuditEntity match;
    protected BetAuditEntity() {}

    private BetAuditEntity(String id,String userId,String betOn,BigDecimal stake,boolean winner,
                           BigDecimal payout,String status,String rejectionReason) {
        this.id=id;this.userId=userId;this.betOn=betOn;this.stake=stake;this.winner=winner;
        this.payout=payout;this.status=status;this.rejectionReason=rejectionReason;
    }

    static BetAuditEntity settled(Bet bet, boolean winner, BigDecimal payout) {
        return new BetAuditEntity(bet.id().value(), bet.userId().value(), bet.team(), bet.stake().amount(),
                winner, payout, winner ? "WON" : "LOST", null);
    }

    static BetAuditEntity rejected(BetPlacedEvent event, String reason) {
        return new BetAuditEntity(event.betId(), event.userId(), event.betOn(), event.amount(),
                false, BigDecimal.ZERO, "REJECTED", reason);
    }
}
