package com.grupo3.betting.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.MatchFinishedEvent;
import com.grupo3.betting.contracts.MatchSimulationCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
class SimulationWorker {
    private static final Logger log = LoggerFactory.getLogger(SimulationWorker.class);
    private final KafkaTemplate<String,String> kafka; private final ObjectMapper mapper;
    SimulationWorker(KafkaTemplate<String,String> kafka, ObjectMapper mapper) { this.kafka = kafka; this.mapper = mapper; }
    @RabbitListener(queues = "match_simulation_queue")
    void simulate(String message) throws Exception {
        var command = mapper.readValue(message, MatchSimulationCommand.class);
        String winner = command.teams().get(ThreadLocalRandom.current().nextInt(command.teams().size()));
        var event = new MatchFinishedEvent(UUID.randomUUID().toString(), command.matchId(), winner, Instant.now());
        kafka.send("match_events", event.matchId(), mapper.writeValueAsString(event)).get();
        log.info("MATCH_FINISHED {} ganador={}", event.matchId(), winner);
    }
}
