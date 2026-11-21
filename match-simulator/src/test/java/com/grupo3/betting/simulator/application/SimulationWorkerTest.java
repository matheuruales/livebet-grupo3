package com.grupo3.betting.simulator.application;

import com.grupo3.betting.simulator.port.FinishedEventPort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.event.MatchFinishedEvent;
import com.grupo3.betting.contracts.command.MatchSimulationCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationWorkerTest {
    @Mock FinishedEventPort events;

    @Test void publishesExactlyOneValidResult() throws Exception {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        var worker = new SimulationWorker(events, mapper);
        worker.simulate(mapper.writeValueAsString(new MatchSimulationCommand("m-1", List.of("A", "B"))));

        var captor = ArgumentCaptor.forClass(MatchFinishedEvent.class);
        verify(events, times(1)).publish(captor.capture());
        assertThat(captor.getValue().matchId()).isEqualTo("m-1");
        assertThat(captor.getValue().winner()).isIn("A", "B");
        assertThat(captor.getValue().homeScore()).isNotEqualTo(captor.getValue().awayScore());
        verifyNoMoreInteractions(events);
    }
}
