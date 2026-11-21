package com.grupo3.betting.engine.application;

import com.grupo3.betting.engine.messaging.NotificationPort;

import com.grupo3.betting.contracts.event.MatchCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class MatchCreatedHandler {
    private static final Logger log = LoggerFactory.getLogger(MatchCreatedHandler.class);
    private final MatchRegistry registry;
    private final NotificationPort notifications;

    MatchCreatedHandler(MatchRegistry registry, NotificationPort notifications) {
        this.registry = registry;
        this.notifications = notifications;
    }

    void handle(MatchCreatedEvent event, boolean firstProcessing) {
        var match = registry.create(event);
        notifications.dashboard("notify.dashboard.created", registry.dashboard(match, event.occurredAt()));
        log.info("Partido {} reconstruido en estado {}", event.matchId(), match.status());
    }
}
