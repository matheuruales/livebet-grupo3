package com.grupo3.betting.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.MatchFinishedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaFinishedEventAdapter implements FinishedEventPort {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    KafkaFinishedEventAdapter(KafkaTemplate<String, String> kafka, ObjectMapper mapper) { this.kafka = kafka; this.mapper = mapper; }
    @Override public void publish(MatchFinishedEvent event) {
        try { kafka.send("match_events", event.matchId(), mapper.writeValueAsString(event)).get(); }
        catch (Exception exception) { throw new IllegalStateException("No se pudo publicar el resultado", exception); }
    }
}
