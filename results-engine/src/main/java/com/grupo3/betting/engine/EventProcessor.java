package com.grupo3.betting.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.*;
import com.grupo3.betting.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
class EventProcessor {
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);
    private final Map<String, MatchAggregate> matches = new ConcurrentHashMap<>();
    private final ObjectMapper mapper; private final MatchAuditRepository matchesRepo; private final ProcessedEventRepository eventsRepo;
    private final NotificationPublisher notifications;
    private final PayoutStrategy payoutStrategy = new FixedOddsPayoutStrategy(BigDecimal.valueOf(2));

    EventProcessor(ObjectMapper mapper, MatchAuditRepository matchesRepo, ProcessedEventRepository eventsRepo, NotificationPublisher notifications) {
        this.mapper=mapper; this.matchesRepo=matchesRepo; this.eventsRepo=eventsRepo; this.notifications=notifications;
    }
    @Transactional
    public void process(String json) throws Exception {
        JsonNode node = mapper.readTree(json); String eventId = node.path("eventId").asText();
        if (eventId.isBlank() || eventsRepo.existsById(eventId)) return;
        if (node.has("teams")) created(mapper.treeToValue(node, MatchCreatedEvent.class));
        else if (node.has("betId")) bet(mapper.treeToValue(node, BetPlacedEvent.class));
        else if (node.has("winner")) finished(mapper.treeToValue(node, MatchFinishedEvent.class));
        else throw new IllegalArgumentException("Contrato de evento desconocido");
    }
    private void created(MatchCreatedEvent event) {
        var match = matches.computeIfAbsent(event.matchId(), id -> MatchAggregate.create(new MatchId(id), event.teams()));
        eventsRepo.save(new ProcessedEventEntity(event.eventId(), "MATCH_CREATED", event.matchId()));
        notifications.dashboard("notify.dashboard.created", dashboard(match, event.occurredAt()));
        log.info("Partido {} reconstruido en estado {}", event.matchId(), match.status());
    }
    private void bet(BetPlacedEvent event) {
        var match = requireMatch(event.matchId());
        try {
            match.placeBet(new Bet(new BetId(event.betId()), new UserId(event.userId()), event.betOn(), new Money(event.amount())));
            notifications.dashboard("notify.dashboard.updated", dashboard(match, event.occurredAt()));
            log.info("Apuesta {} aceptada para {}", event.betId(), event.matchId());
        } catch (RuntimeException ex) {
            notifications.email("notify.email.rejected", new EmailNotification(UUID.randomUUID().toString(), event.userId(), event.matchId(), "REJECTED", event.amount()));
            log.warn("Apuesta {} rechazada: {}", event.betId(), ex.getMessage());
        }
        eventsRepo.save(new ProcessedEventEntity(event.eventId(), "BET_PLACED", event.matchId()));
    }
    private void finished(MatchFinishedEvent event) {
        var match = requireMatch(event.matchId()); match.finish(event.winner()); match.settle(payoutStrategy);
        var audit = new MatchAuditEntity(match.id().value(), match.teams().get(0), match.teams().get(1), match.status().name(), match.winner(), match.totalBet(), Instant.now());
        for (var settlement : match.settlements()) {
            var bet=settlement.bet(); audit.add(new BetAuditEntity(bet.id().value(),bet.userId().value(),bet.team(),bet.stake().amount(),settlement.winner(),settlement.payout().amount()));
            notifications.email(settlement.winner()?"notify.email.win":"notify.email.loss",
                    new EmailNotification(UUID.randomUUID().toString(),bet.userId().value(),match.id().value(),settlement.winner()?"WIN":"LOSS",settlement.winner()?settlement.payout().amount():bet.stake().amount()));
        }
        matchesRepo.save(audit); eventsRepo.save(new ProcessedEventEntity(event.eventId(), "MATCH_FINISHED", event.matchId()));
        notifications.dashboard("notify.dashboard.finished", dashboard(match, event.occurredAt()));
        log.info("Partido {} liquidado, ganador={}, apuestas={}", match.id().value(), match.winner(), match.bets().size());
    }
    private MatchAggregate requireMatch(String id) {
        var match=matches.get(id); if(match==null) throw new IllegalStateException("No existe el partido " + id); return match;
    }
    private DashboardNotification dashboard(MatchAggregate match, Instant at) {
        return new DashboardNotification(match.id().value(),match.status().name(),match.teams(),match.winner(),match.bets().size(),match.totalBet(),at);
    }
    Map<String,MatchAggregate> snapshot() { return Map.copyOf(matches); }
}
