package com.grupo3.betting.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.BetPlacedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class BetPublisher {
    private final KafkaTemplate<String,String> kafka; private final ObjectMapper mapper;
    BetPublisher(KafkaTemplate<String,String> kafka, ObjectMapper mapper) { this.kafka = kafka; this.mapper = mapper; }
    void publish(BetPlacedEvent event) {
        try { kafka.send("match_events", event.matchId(), mapper.writeValueAsString(event)); }
        catch (Exception e) { throw new IllegalStateException("No se pudo publicar la apuesta", e); }
    }
}
