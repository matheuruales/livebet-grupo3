package com.grupo3.betting.api.bet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.event.BetPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class BetOutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(BetOutboxDispatcher.class);
    private final BetOutboxRepository outbox;
    private final BetEventPublisherPort publisher;
    private final ObjectMapper mapper;

    BetOutboxDispatcher(BetOutboxRepository outbox, BetEventPublisherPort publisher, ObjectMapper mapper) {
        this.outbox = outbox; this.publisher = publisher; this.mapper = mapper;
    }

    @Scheduled(initialDelay = 500, fixedDelay = 500)
    @Transactional
    void publishPending() {
        for (BetOutboxEntity item : outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                publisher.publish(mapper.readValue(item.payload(), BetPlacedEvent.class));
                item.published();
            } catch (Exception exception) {
                item.failed(exception);
                log.warn("Evento de apuesta pendiente; se reintentará: {}", exception.getMessage());
            }
        }
    }
}
