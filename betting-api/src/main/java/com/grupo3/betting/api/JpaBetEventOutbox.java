package com.grupo3.betting.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.BetPlacedEvent;
import org.springframework.stereotype.Component;

@Component
class JpaBetEventOutbox implements BetEventOutboxPort {
    private final BetOutboxRepository outbox;
    private final ObjectMapper mapper;

    JpaBetEventOutbox(BetOutboxRepository outbox, ObjectMapper mapper) {
        this.outbox = outbox; this.mapper = mapper;
    }

    @Override public void enqueue(BetPlacedEvent event) {
        try {
            outbox.save(new BetOutboxEntity(event.eventId(), event.matchId(), mapper.writeValueAsString(event)));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No se pudo registrar el evento de la apuesta", exception);
        }
    }
}
