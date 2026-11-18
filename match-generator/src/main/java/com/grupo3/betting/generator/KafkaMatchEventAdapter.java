package com.grupo3.betting.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.MatchCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaMatchEventAdapter implements MatchEventPort {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    KafkaMatchEventAdapter(KafkaTemplate<String, String> kafka, ObjectMapper mapper) { this.kafka = kafka; this.mapper = mapper; }
    @Override public void publish(MatchCreatedEvent event) {
        try { kafka.send("match_events", event.matchId(), mapper.writeValueAsString(event)).get(); }
        catch (Exception exception) { throw new IllegalStateException("No se pudo publicar el partido", exception); }
    }
}
