package com.grupo3.betting.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class KafkaEventListener {
    private static final Logger log= LoggerFactory.getLogger(KafkaEventListener.class);
    private final EventProcessor processor;
    KafkaEventListener(EventProcessor processor){this.processor=processor;}
    @KafkaListener(topics="match_events", groupId="${app.kafka-group:results-engine-v1}")
    void consume(String json) {
        try { processor.process(json); }
        catch (Exception e) { log.error("Evento rechazado sin detener el flujo: {}", e.getMessage(), e); }
    }
}
