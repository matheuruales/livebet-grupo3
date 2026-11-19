package com.grupo3.betting.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
class MatchStateRecovery {
    private static final Logger log = LoggerFactory.getLogger(MatchStateRecovery.class);
    private final ProcessedEventRepository events;
    private final EventProcessor processor;

    MatchStateRecovery(ProcessedEventRepository events, EventProcessor processor) {
        this.events = events;
        this.processor = processor;
    }

    @Order(0)
    @EventListener(ApplicationReadyEvent.class)
    void recover() {
        int recovered = 0;
        for (ProcessedEventEntity event : events.findEventsForUnfinishedMatches()) {
            try {
                processor.process(event.payload());
                recovered++;
            } catch (Exception exception) {
                log.error("No se pudo recuperar un evento pendiente: {}", exception.getMessage(), exception);
            }
        }
        log.info("Estado de partidos recuperado: {} eventos pendientes", recovered);
    }
}
