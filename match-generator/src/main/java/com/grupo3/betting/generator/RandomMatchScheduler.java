package com.grupo3.betting.generator;

import com.grupo3.betting.contracts.MatchCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
class RandomMatchScheduler {
    private static final Logger log = LoggerFactory.getLogger(RandomMatchScheduler.class);
    private static final List<String> TEAMS = List.of("Cúcuta", "Pasto", "Cali", "Nacional", "Millonarios", "Tolima", "Junior", "América");
    private final MatchEventPublisher publisher;
    RandomMatchScheduler(MatchEventPublisher publisher) { this.publisher = publisher; }
    @Scheduled(initialDelayString = "${app.initial-delay-ms:8000}", fixedDelayString = "${app.generation-interval-ms:45000}")
    void generate() {
        int first = ThreadLocalRandom.current().nextInt(TEAMS.size());
        int second; do { second = ThreadLocalRandom.current().nextInt(TEAMS.size()); } while (second == first);
        var startsAt = Instant.now();
        var event = new MatchCreatedEvent(UUID.randomUUID().toString(), "m-" + UUID.randomUUID().toString().substring(0, 8),
                List.of(TEAMS.get(first), TEAMS.get(second)), startsAt,
                startsAt.plusMillis(publisher.matchDurationMs()));
        publisher.publish(event); log.info("MATCH_CREATED {}: {} vs {}", event.matchId(), event.teams().get(0), event.teams().get(1));
    }
}
