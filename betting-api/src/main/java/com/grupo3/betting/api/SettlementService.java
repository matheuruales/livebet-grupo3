package com.grupo3.betting.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
class SettlementService {
    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);
    private final BetRequestRepository bets;
    private final UserAccountRepository users;

    SettlementService(BetRequestRepository bets, UserAccountRepository users) {
        this.bets = bets;
        this.users = users;
    }

    @Transactional
    public void settle(String betId, String expectedUserId, boolean winner, BigDecimal payout) {
        var bet = bets.findByIdForSettlement(betId).orElse(null);
        if (bet == null || !bet.pending()) return;
        if (!bet.userId().equals(expectedUserId)) throw new IllegalStateException("La apuesta no pertenece al usuario esperado");
        if (winner && (payout == null || payout.signum() <= 0)) throw new IllegalArgumentException("El premio debe ser positivo");
        if (winner) users.findById(expectedUserId)
                .orElseThrow(() -> new IllegalStateException("Usuario de la apuesta no encontrado"))
                .credit(payout);
        bet.settle(winner, winner ? payout : BigDecimal.ZERO, Instant.now());
        log.info("Apuesta {} liquidada como {}, premio={}", betId, winner ? "GANADA" : "PERDIDA", winner ? payout : BigDecimal.ZERO);
    }
}

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
                settlements.settle(settlement.getBetId(), settlement.getUserId(),
                        Boolean.TRUE.equals(settlement.getWinner()), settlement.getPayout());
            } catch (RuntimeException ex) {
                log.error("No se pudo liquidar la apuesta {}: {}", settlement.getBetId(), ex.getMessage());
            }
        }
    }
}
