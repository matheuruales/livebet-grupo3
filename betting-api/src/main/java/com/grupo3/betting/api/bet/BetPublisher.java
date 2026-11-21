package com.grupo3.betting.api.bet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.event.BetPlacedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class BetPublisher implements BetEventPublisherPort {
    private final KafkaTemplate<String,String> kafka; private final ObjectMapper mapper;
    BetPublisher(KafkaTemplate<String,String> kafka, ObjectMapper mapper) { this.kafka = kafka; this.mapper = mapper; }
    @Override public void publish(BetPlacedEvent event) {
        try { kafka.send("match_events", event.matchId(), mapper.writeValueAsString(event)).get(); }
        catch (Exception e) { throw new IllegalStateException("No se pudo publicar la apuesta", e); }
    }
}
