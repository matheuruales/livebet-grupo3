package com.grupo3.betting.engine;

import com.grupo3.betting.contracts.BetPlacedEvent;
import com.grupo3.betting.contracts.EmailNotification;
import com.grupo3.betting.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class BetPlacedHandler {
    private static final Logger log = LoggerFactory.getLogger(BetPlacedHandler.class);
    private final MatchRegistry registry;
    private final BetAuditRepository audits;
    private final NotificationPort notifications;

    BetPlacedHandler(MatchRegistry registry, BetAuditRepository audits, NotificationPort notifications) {
        this.registry = registry;
        this.audits = audits;
        this.notifications = notifications;
    }

    void handle(BetPlacedEvent event, boolean firstProcessing) {
        var match = registry.require(event.matchId());
        try {
            match.placeBet(new Bet(new BetId(event.betId()), new UserId(event.userId()), event.betOn(), new Money(event.amount())));
            notifications.dashboard("notify.dashboard.updated", registry.dashboard(match, event.occurredAt()));
            log.info("Apuesta {} aceptada para {}", event.betId(), event.matchId());
        } catch (RuntimeException exception) {
            if (firstProcessing) {
                audits.save(BetAuditEntity.rejected(event, exception.getMessage()));
                notifications.email("notify.email.rejected", new EmailNotification(UUID.randomUUID().toString(),
                        event.userId(), event.matchId(), "REJECTED", event.amount()));
            }
            log.warn("Apuesta {} rechazada: {}", event.betId(), exception.getMessage());
        }
    }
}
