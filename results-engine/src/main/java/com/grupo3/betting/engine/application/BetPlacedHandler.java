package com.grupo3.betting.engine.application;

import com.grupo3.betting.engine.messaging.NotificationPort;
import com.grupo3.betting.engine.persistence.BetAuditEntity;
import com.grupo3.betting.engine.persistence.BetAuditRepository;

import com.grupo3.betting.contracts.event.BetPlacedEvent;
import com.grupo3.betting.contracts.notification.EmailNotification;
import com.grupo3.betting.domain.bet.Bet;
import com.grupo3.betting.domain.bet.BetId;
import com.grupo3.betting.domain.match.MatchAggregate;
import com.grupo3.betting.domain.money.Money;
import com.grupo3.betting.domain.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class BetPlacedHandler {
    private static final Logger log = LoggerFactory.getLogger(BetPlacedHandler.class);
    private final MatchRegistry registry;
    private final BetContactRegistry contacts;
    private final BetAuditRepository audits;
    private final NotificationPort notifications;

    BetPlacedHandler(MatchRegistry registry, BetContactRegistry contacts, BetAuditRepository audits,
                     NotificationPort notifications) {
        this.registry = registry;
        this.contacts = contacts;
        this.audits = audits;
        this.notifications = notifications;
    }

    void handle(BetPlacedEvent event, boolean firstProcessing) {
        var match = registry.require(event.matchId());
        contacts.remember(event.betId(), event.userEmail());
        try {
            match.placeBet(new Bet(new BetId(event.betId()), new UserId(event.userId()), event.betOn(), new Money(event.amount())));
            notifications.dashboard("notify.dashboard.updated", registry.dashboard(match, event.occurredAt()));
            log.info("Apuesta {} aceptada para {}", event.betId(), event.matchId());
        } catch (RuntimeException exception) {
            if (firstProcessing) {
                audits.save(BetAuditEntity.rejected(event, exception.getMessage()));
                notifications.email("notify.email.rejected", new EmailNotification(UUID.randomUUID().toString(),
                        event.userEmail(), event.userId(), event.betId(), event.matchId(), "REJECTED",
                        match.teams().get(0), match.teams().get(1), event.betOn(), null, null, null,
                        event.amount(), event.amount(), event.occurredAt()));
            }
            log.warn("Apuesta {} rechazada: {}", event.betId(), exception.getMessage());
        }
    }
}
