package com.grupo3.betting.generator.application;

import com.grupo3.betting.generator.port.MatchEventPort;
import com.grupo3.betting.generator.port.MatchTimerPort;

import com.grupo3.betting.contracts.event.MatchCreatedEvent;
import com.grupo3.betting.contracts.command.MatchSimulationCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class MatchEventPublisher {
    private final MatchEventPort events;
    private final MatchTimerPort timers;
    private final long durationMs;
    MatchEventPublisher(MatchEventPort events, MatchTimerPort timers,
                        @Value("${app.match-duration-ms:60000}") long durationMs) {
        this.events = events; this.timers = timers; this.durationMs = durationMs;
    }
    long matchDurationMs() { return durationMs; }
    void publish(MatchCreatedEvent event) {
        events.publish(event);
        timers.schedule(new MatchSimulationCommand(event.matchId(), event.teams()), event.eventId(), durationMs);
    }
}
