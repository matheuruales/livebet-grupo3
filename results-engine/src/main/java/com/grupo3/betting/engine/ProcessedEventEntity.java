package com.grupo3.betting.engine;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="processed_events")
class ProcessedEventEntity {
    @Id String eventId; String eventType; String matchId; Instant processedAt;
    protected ProcessedEventEntity() {}
    ProcessedEventEntity(String eventId,String eventType,String matchId) { this.eventId=eventId;this.eventType=eventType;this.matchId=matchId;this.processedAt=Instant.now(); }
}
