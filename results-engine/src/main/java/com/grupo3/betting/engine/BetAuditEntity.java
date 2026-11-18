package com.grupo3.betting.engine;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="bets")
class BetAuditEntity {
    @Id String id; String userId; String betOn; BigDecimal stake; boolean winner; BigDecimal payout;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="match_id") MatchAuditEntity match;
    protected BetAuditEntity() {}
    BetAuditEntity(String id,String userId,String betOn,BigDecimal stake,boolean winner,BigDecimal payout) {
        this.id=id; this.userId=userId; this.betOn=betOn; this.stake=stake; this.winner=winner; this.payout=payout;
    }
}
