package com.grupo3.betting.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.DashboardNotification;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StateControllerTest {
    @Test void excludesOpenMatchesThatExpiredBeyondTheSettlementGracePeriod() throws Exception {
        var listener = new DashboardListener(mock(DashboardSocketHandler.class), new ObjectMapper().findAndRegisterModules());
        Instant now = Instant.now();
        listener.update(new DashboardNotification("stale", "OPEN_FOR_BETS", List.of("A", "B"), null,
                null, null, 0, BigDecimal.ZERO, now.minusSeconds(60), now.minusSeconds(20), now));
        listener.update(new DashboardNotification("current", "OPEN_FOR_BETS", List.of("C", "D"), null,
                null, null, 0, BigDecimal.ZERO, now, now.plusSeconds(30), now));
        listener.update(new DashboardNotification("finished", "SETTLED", List.of("E", "F"), "E",
                1, 0, 0, BigDecimal.ZERO, now.minusSeconds(60), now.minusSeconds(30), now));

        var result = new StateController(listener).matches();

        assertThat(result).extracting(DashboardNotification::matchId)
                .containsExactlyInAnyOrder("current", "finished");
    }
}
