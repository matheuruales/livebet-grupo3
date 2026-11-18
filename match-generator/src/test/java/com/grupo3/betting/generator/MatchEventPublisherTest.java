package com.grupo3.betting.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.MatchCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.Instant;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MatchEventPublisherTest {
    @Test void sendsEventAndSchedulesSimulation() {
        KafkaTemplate<String,String> kafka = mock(KafkaTemplate.class); RabbitTemplate rabbit = mock(RabbitTemplate.class);
        var publisher = new MatchEventPublisher(kafka, rabbit, new ObjectMapper().findAndRegisterModules(), 1000);
        var start = Instant.now();
        publisher.publish(new MatchCreatedEvent("e1", "m1", List.of("A", "B"), start, start.plusSeconds(1)));
        verify(kafka).send(eq("match_events"), eq("m1"), anyString());
        verify(rabbit).send(eq("match.delayed"), eq("match.simulate"), any());
    }
}
