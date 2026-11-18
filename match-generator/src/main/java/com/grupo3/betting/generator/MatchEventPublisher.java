package com.grupo3.betting.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.MatchCreatedEvent;
import com.grupo3.betting.contracts.MatchSimulationCommand;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class MatchEventPublisher {
    private final KafkaTemplate<String, String> kafka;
    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;
    private final long durationMs;
    MatchEventPublisher(KafkaTemplate<String, String> kafka, RabbitTemplate rabbit, ObjectMapper mapper,
                        @Value("${app.match-duration-ms:60000}") long durationMs) {
        this.kafka = kafka; this.rabbit = rabbit; this.mapper = mapper; this.durationMs = durationMs;
    }
    long matchDurationMs() { return durationMs; }
    void publish(MatchCreatedEvent event) {
        try {
            kafka.send("match_events", event.matchId(), mapper.writeValueAsString(event));
            var body = mapper.writeValueAsBytes(new MatchSimulationCommand(event.matchId(), event.teams()));
            var properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setHeader("x-delay", durationMs);
            properties.setMessageId(event.eventId());
            rabbit.send(MessagingConfig.DELAYED_EXCHANGE, "match.simulate", new Message(body, properties));
        } catch (JsonProcessingException e) { throw new IllegalStateException("No se pudo serializar el partido", e); }
    }
}
