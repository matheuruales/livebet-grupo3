package com.grupo3.betting.generator.application;

import com.grupo3.betting.generator.port.MatchEventPort;
import com.grupo3.betting.generator.port.MatchTimerPort;

import com.grupo3.betting.contracts.event.MatchCreatedEvent;
import com.grupo3.betting.contracts.command.MatchSimulationCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchEventPublisherTest {
    @Mock MatchEventPort events;
    @Mock MatchTimerPort timers;

    @Test void sendsEventAndSchedulesSimulation() {
        var publisher = new MatchEventPublisher(events, timers, 1000);
        var start = Instant.now();
        var event = new MatchCreatedEvent("e1", "m1", List.of("A", "B"), start, start.plusSeconds(1));
        publisher.publish(event);
        verify(events, times(1)).publish(event);
        verify(timers, times(1)).schedule(any(MatchSimulationCommand.class), eq("e1"), eq(1000L));
        verifyNoMoreInteractions(events, timers);
    }
}
