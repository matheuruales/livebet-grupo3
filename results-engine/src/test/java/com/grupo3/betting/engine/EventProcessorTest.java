package com.grupo3.betting.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventProcessorTest {
    @Mock MatchAuditRepository matches;
    @Mock BetAuditRepository audits;
    @Mock ProcessedEventRepository events;
    @Mock NotificationPort notifications;
    private ObjectMapper mapper;
    private EventProcessor processor;
    private MatchRegistry registry;

    @BeforeEach void setup(){
        mapper = new ObjectMapper().findAndRegisterModules();
        registry = new MatchRegistry();
        processor = new EventProcessor(mapper, events,
                new MatchCreatedHandler(registry, notifications),
                new BetPlacedHandler(registry, audits, notifications),
                new MatchFinishedHandler(registry, matches, notifications));
    }
    @Test void persistsAndNotifiesAfterSettlement() throws Exception {
        var start=Instant.now();
        process(new MatchCreatedEvent("e1","m1",List.of("A","B"),start,start.plusSeconds(60)));
        process(new BetPlacedEvent("e2","b1","u1","m1","A",BigDecimal.TEN,Instant.now()));
        process(new MatchFinishedEvent("e3","m1","A",2,1,Instant.now()));
        verify(matches).save(any(MatchAuditEntity.class));
        verify(notifications).email(eq("notify.email.win"),any(EmailNotification.class));
        verify(notifications).dashboard(eq("notify.dashboard.finished"),any(DashboardNotification.class));
    }
    @Test void rejectsBetAfterCloseWithoutBreakingStream() throws Exception {
        var start=Instant.now();
        process(new MatchCreatedEvent("e1","m1",List.of("A","B"),start,start.plusSeconds(60)));
        process(new MatchFinishedEvent("e2","m1","B",0,3,Instant.now()));
        process(new BetPlacedEvent("e3","b1","u1","m1","A",BigDecimal.TEN,Instant.now()));
        verify(notifications).email(eq("notify.email.rejected"),any(EmailNotification.class));
        verify(audits).save(any(BetAuditEntity.class));
    }
    @Test void reconstructsStateFromAlreadyProcessedKafkaEventsWithoutDuplicatingSideEffects() throws Exception {
        when(events.existsById(anyString())).thenReturn(true);
        var start = Instant.now();
        process(new MatchCreatedEvent("e1","m1",List.of("A","B"),start,start.plusSeconds(60)));
        process(new BetPlacedEvent("e2","b1","u1","m1","A",BigDecimal.TEN,start.plusSeconds(5)));
        process(new MatchFinishedEvent("e3","m1","A",2,1,start.plusSeconds(60)));

        assert registry.require("m1").status() == com.grupo3.betting.domain.MatchStatus.SETTLED;
        assert registry.require("m1").bets().size() == 1;
        verify(notifications, times(3)).dashboard(anyString(), any(DashboardNotification.class));
        verify(notifications, never()).email(anyString(), any(EmailNotification.class));
        verify(matches, never()).save(any());
    }
    private void process(Object event) throws Exception {processor.process(mapper.writeValueAsString(event));}
}
