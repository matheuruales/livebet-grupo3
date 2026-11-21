package com.grupo3.betting.dashboard.messaging;

import com.grupo3.betting.dashboard.websocket.DashboardSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.notification.DashboardNotification;
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

    @Test void neverReopensAFinalizedMatchWithAnOldNotification() throws Exception {
        var listener = new DashboardListener(sockets, new ObjectMapper().findAndRegisterModules());
        Instant now = Instant.now();
        var settled = new DashboardNotification("m-1", "SETTLED", List.of("A", "B"), "A",
                2, 1, 1, BigDecimal.TEN, now.minusSeconds(60), now, now);
        var staleOpen = new DashboardNotification("m-1", "OPEN_FOR_BETS", List.of("A", "B"), null,
                null, null, 0, BigDecimal.ZERO, now.minusSeconds(60), now, now.minusSeconds(60));

        listener.update(settled);
        listener.update(staleOpen);

        assertThat(listener.state().get("m-1").status()).isEqualTo("SETTLED");
        verify(sockets, times(1)).broadcast(contains("m-1"));
    }

    @Test void keepsOnlyACompactWindowOfFinishedMatches() throws Exception {
        var listener = new DashboardListener(sockets, new ObjectMapper().findAndRegisterModules());
        Instant now = Instant.now();
        for (int index = 0; index < 80; index++) {
            listener.update(new DashboardNotification("m-" + index, "SETTLED", List.of("A", "B"), "A",
                    1, 0, 0, BigDecimal.ZERO, now.minusSeconds(120), now.minusSeconds(60), now.plusSeconds(index)));
        }

        assertThat(listener.state()).hasSizeLessThanOrEqualTo(60);
        assertThat(listener.state()).containsKey("m-79");
    }
}
