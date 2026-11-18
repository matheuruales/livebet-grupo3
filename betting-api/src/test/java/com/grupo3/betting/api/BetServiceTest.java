package com.grupo3.betting.api;

import com.grupo3.betting.contracts.BetPlacedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetServiceTest {
    @Mock UserAccountRepository users;
    @Mock BetRequestRepository bets;
    @Mock BetEventOutboxPort outbox;
    @InjectMocks BetService service;

    @Test void debitsPersistsAndEnqueuesOneEvent() {
        var user = new UserAccount("Matheu", "matheu@example.com", "hash", UserAccount.Role.USER, new BigDecimal("250000"));
        when(users.findById(user.id())).thenReturn(Optional.of(user));

        PlaceBetResponse response = service.place(user.id(), new PlaceBetRequest("m-1", "Cali", new BigDecimal("5000")));

        assertThat(response.balance()).isEqualByComparingTo("245000");
        verify(bets, times(1)).save(any(BetRequestEntity.class));
        var event = ArgumentCaptor.forClass(BetPlacedEvent.class);
        verify(outbox, times(1)).enqueue(event.capture());
        assertThat(event.getValue().eventType().name()).isEqualTo("BET_PLACED");
        verifyNoMoreInteractions(outbox);
    }
}
