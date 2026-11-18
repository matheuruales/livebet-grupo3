package com.grupo3.betting.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class SettlementReconciler {
    private static final Logger log = LoggerFactory.getLogger(SettlementReconciler.class);
    private final BetRequestRepository bets;
    private final SettlementService settlements;

    SettlementReconciler(BetRequestRepository bets, SettlementService settlements) {
        this.bets = bets;
        this.settlements = settlements;
    }

    @Scheduled(initialDelay = 3000, fixedDelay = 2000)
    void reconcile() {
        for (var settlement : bets.findPendingSettlements()) {
            try {
                settlements.reconcile(settlement.getBetId(), settlement.getUserId(),
                        settlement.getStatus(), settlement.getPayout());
            } catch (RuntimeException exception) {
                log.error("No se pudo conciliar la apuesta {}: {}", settlement.getBetId(), exception.getMessage());
            }
        }
    }
}
