package com.grupo3.betting.engine.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.grupo3.betting.contracts.event.BetPlacedEvent;
import com.grupo3.betting.contracts.event.EventType;
import com.grupo3.betting.contracts.event.MatchCreatedEvent;
import com.grupo3.betting.contracts.event.MatchFinishedEvent;
import com.grupo3.betting.engine.persistence.ProcessedEventEntity;
import com.grupo3.betting.engine.persistence.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventProcessor {
    private final ObjectMapper mapper;
    private final ProcessedEventRepository processedEvents;
    private final MatchCreatedHandler createdHandler;
    private final BetPlacedHandler betHandler;
    private final MatchFinishedHandler finishedHandler;

    public EventProcessor(ObjectMapper mapper, ProcessedEventRepository processedEvents,
                   MatchCreatedHandler createdHandler, BetPlacedHandler betHandler,
                   MatchFinishedHandler finishedHandler) {
        this.mapper = mapper;
        this.processedEvents = processedEvents;
        this.createdHandler = createdHandler;
        this.betHandler = betHandler;
        this.finishedHandler = finishedHandler;
    }

    @Transactional
    public void process(String json) throws Exception {
        JsonNode parsed = mapper.readTree(json);
        String eventId = parsed.path("eventId").asText();
        if (eventId.isBlank()) throw new IllegalArgumentException("El evento no tiene eventId");
        EventType type = resolveType(parsed);
        ObjectNode normalized = (ObjectNode) parsed;
        normalized.put("eventType", type.name());
        boolean firstProcessing = !processedEvents.existsById(eventId);

        switch (type) {
            case MATCH_CREATED -> createdHandler.handle(mapper.treeToValue(normalized, MatchCreatedEvent.class), firstProcessing);
            case BET_PLACED -> betHandler.handle(mapper.treeToValue(normalized, BetPlacedEvent.class), firstProcessing);
            case MATCH_FINISHED -> finishedHandler.handle(mapper.treeToValue(normalized, MatchFinishedEvent.class), firstProcessing);
        }
        if (firstProcessing) {
            processedEvents.save(new ProcessedEventEntity(eventId, type.name(), parsed.path("matchId").asText(), json));
        }
    }

    private EventType resolveType(JsonNode event) {
        if (event.hasNonNull("eventType")) return EventType.valueOf(event.get("eventType").asText());
        if (event.has("teams")) return EventType.MATCH_CREATED;
        if (event.has("betId")) return EventType.BET_PLACED;
        if (event.has("winner")) return EventType.MATCH_FINISHED;
        throw new IllegalArgumentException("Contrato de evento desconocido");
    }
}
