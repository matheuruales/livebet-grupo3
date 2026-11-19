package com.grupo3.betting.engine;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="processed_events")
class ProcessedEventEntity {
    @Id String eventId; String eventType; String matchId; Instant processedAt;
    @Column(columnDefinition="TEXT") String payload;
    protected ProcessedEventEntity() {}
    ProcessedEventEntity(String eventId,String eventType,String matchId,String payload) {
        this.eventId=eventId;this.eventType=eventType;this.matchId=matchId;this.payload=payload;this.processedAt=Instant.now();
    }
    String payload() { return payload; }
}
