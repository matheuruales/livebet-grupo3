package com.grupo3.betting.engine.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="processed_events")
public class ProcessedEventEntity {
    @Id String eventId; String eventType; String matchId; Instant processedAt;
    @Column(columnDefinition="TEXT") String payload;
    protected ProcessedEventEntity() {}
    public ProcessedEventEntity(String eventId,String eventType,String matchId,String payload) {
        this.eventId=eventId;this.eventType=eventType;this.matchId=matchId;this.payload=payload;this.processedAt=Instant.now();
    }
    public String payload() { return payload; }
}
