package com.grupo3.betting.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.DashboardNotification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardListenerTest {
    @Mock DashboardSocketHandler sockets;

    @Test void storesAndBroadcastsEveryUpdateExactlyOnce() throws Exception {
        var listener = new DashboardListener(sockets, new ObjectMapper().findAndRegisterModules());
        var notification = new DashboardNotification("m-1", "OPEN_FOR_BETS", List.of("A", "B"), null,
                null, null, 0, BigDecimal.ZERO, Instant.now(), Instant.now().plusSeconds(60), Instant.now());

        listener.update(notification);

        assertThat(listener.state()).containsEntry("m-1", notification);
        verify(sockets, times(1)).broadcast(contains("m-1"));
        verifyNoMoreInteractions(sockets);
    }
}
