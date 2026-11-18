package com.grupo3.betting.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.MatchFinishedEvent;
import com.grupo3.betting.contracts.MatchSimulationCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
class SimulationWorker {
    private static final Logger log = LoggerFactory.getLogger(SimulationWorker.class);
    private final FinishedEventPort events; private final ObjectMapper mapper;
    SimulationWorker(FinishedEventPort events, ObjectMapper mapper) { this.events = events; this.mapper = mapper; }
    @RabbitListener(queues = "match_simulation_queue")
    void simulate(String message) throws Exception {
        var command = mapper.readValue(message, MatchSimulationCommand.class);
        int winnerIndex = ThreadLocalRandom.current().nextInt(command.teams().size());
        int loserScore = ThreadLocalRandom.current().nextInt(0, 4);
        int winnerScore = ThreadLocalRandom.current().nextInt(loserScore + 1, 6);
        int homeScore = winnerIndex == 0 ? winnerScore : loserScore;
        int awayScore = winnerIndex == 1 ? winnerScore : loserScore;
        String winner = command.teams().get(winnerIndex);
        var event = new MatchFinishedEvent(UUID.randomUUID().toString(), command.matchId(), winner,
                homeScore, awayScore, Instant.now());
        events.publish(event);
        log.info("MATCH_FINISHED {} marcador={}-{} ganador={}", event.matchId(), homeScore, awayScore, winner);
    }
}
