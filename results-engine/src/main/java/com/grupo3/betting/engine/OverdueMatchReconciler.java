package com.grupo3.betting.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.MatchFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
class OverdueMatchReconciler {
    private static final Logger log = LoggerFactory.getLogger(OverdueMatchReconciler.class);
    private final MatchRegistry registry;
    private final EventProcessor processor;
    private final ObjectMapper mapper;

    OverdueMatchReconciler(MatchRegistry registry, EventProcessor processor, ObjectMapper mapper) {
        this.registry = registry;
        this.processor = processor;
        this.mapper = mapper;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    void closeOverdueMatches() {
        for (MatchRegistry.OverdueMatch overdue : registry.overdue(Instant.now())) {
            try {
                int winnerIndex = ThreadLocalRandom.current().nextInt(2);
                int loserScore = ThreadLocalRandom.current().nextInt(0, 4);
                int winnerScore = ThreadLocalRandom.current().nextInt(loserScore + 1, 6);
                var event = new MatchFinishedEvent(UUID.randomUUID().toString(), overdue.matchId(),
                        overdue.teams().get(winnerIndex), winnerIndex == 0 ? winnerScore : loserScore,
                        winnerIndex == 1 ? winnerScore : loserScore, Instant.now());
                processor.process(mapper.writeValueAsString(event));
                log.warn("Partido {} cerrado por el reconciliador de respaldo", overdue.matchId());
            } catch (Exception exception) {
                log.error("No se pudo cerrar el partido vencido {}: {}", overdue.matchId(),
                        exception.getMessage(), exception);
            }
        }
    }
}
