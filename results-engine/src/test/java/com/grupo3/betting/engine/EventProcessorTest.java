package com.grupo3.betting.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventProcessorTest {
    private MatchAuditRepository matches; private ProcessedEventRepository events; private NotificationPublisher notifications;
    private ObjectMapper mapper; private EventProcessor processor;
    @BeforeEach void setup(){
        matches=mock(MatchAuditRepository.class);events=mock(ProcessedEventRepository.class);notifications=mock(NotificationPublisher.class);
        mapper=new ObjectMapper().findAndRegisterModules();processor=new EventProcessor(mapper,matches,events,notifications);
    }
    @Test void persistsAndNotifiesAfterSettlement() throws Exception {
        process(new MatchCreatedEvent("e1","m1",List.of("A","B"),Instant.now()));
        process(new BetPlacedEvent("e2","b1","u1","m1","A",BigDecimal.TEN,Instant.now()));
        process(new MatchFinishedEvent("e3","m1","A",Instant.now()));
        verify(matches).save(any(MatchAuditEntity.class));
        verify(notifications).email(eq("notify.email.win"),any(EmailNotification.class));
        verify(notifications).dashboard(eq("notify.dashboard.finished"),any(DashboardNotification.class));
    }
    @Test void rejectsBetAfterCloseWithoutBreakingStream() throws Exception {
        process(new MatchCreatedEvent("e1","m1",List.of("A","B"),Instant.now()));
        process(new MatchFinishedEvent("e2","m1","B",Instant.now()));
        process(new BetPlacedEvent("e3","b1","u1","m1","A",BigDecimal.TEN,Instant.now()));
        verify(notifications).email(eq("notify.email.rejected"),any(EmailNotification.class));
    }
    private void process(Object event) throws Exception {processor.process(mapper.writeValueAsString(event));}
}
