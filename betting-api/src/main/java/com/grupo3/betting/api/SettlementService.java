package com.grupo3.betting.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Transactional
    public void reconcile(String betId, String expectedUserId, String status, BigDecimal payout) {
        var bet = bets.findByIdForSettlement(betId).orElse(null);
        if (bet == null || !bet.pending()) return;
        if (!bet.userId().equals(expectedUserId)) throw new IllegalStateException("La apuesta no pertenece al usuario esperado");
        if ("REJECTED".equals(status)) {
            users.findById(expectedUserId).orElseThrow(() -> new IllegalStateException("Usuario no encontrado")).credit(bet.amount());
            bet.reject(Instant.now());
            log.info("Apuesta {} rechazada y reembolsada", betId);
            return;
        }
        settle(betId, expectedUserId, "WON".equals(status), payout);
    }
}
