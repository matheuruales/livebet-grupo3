package com.grupo3.betting.api.bet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.event.BetPlacedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.List;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetOutboxDispatcherTest {
    @Mock BetOutboxRepository outbox;
    @Mock BetEventPublisherPort publisher;

    @Test void publishesPendingEventExactlyOnce() throws Exception {
        var mapper = new ObjectMapper().findAndRegisterModules();
        var event = new BetPlacedEvent("e-1", "b-1", "u-1", "m-1", "Cali", BigDecimal.TEN, Instant.now());
        var item = new BetOutboxEntity(event.eventId(), event.matchId(), mapper.writeValueAsString(event));
        when(outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(item));

        new BetOutboxDispatcher(outbox, publisher, mapper).publishPending();

        verify(publisher, times(1)).publish(event);
        verifyNoMoreInteractions(publisher);
    }
}
