package com.grupo3.betting.engine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, String> {
    @Query("""
            select event from ProcessedEventEntity event
            where event.matchId not in (
                select finished.matchId from ProcessedEventEntity finished
                where finished.eventType = 'MATCH_FINISHED'
            )
            order by event.processedAt
            """)
    List<ProcessedEventEntity> findEventsForUnfinishedMatches();
}
