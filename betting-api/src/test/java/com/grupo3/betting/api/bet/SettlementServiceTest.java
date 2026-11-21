package com.grupo3.betting.api.bet;

import com.grupo3.betting.api.account.UserAccount;
import com.grupo3.betting.api.account.UserAccountRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {
    @Mock BetRequestRepository bets;
    @Mock UserAccountRepository users;
    @InjectMocks SettlementService service;

    @Test
    void acreditaElPremioUnaSolaVezYMarcaLaApuestaGanada() {
        var bet = new BetRequestEntity("b-1", "u-1", "m-1", "Cali", new BigDecimal("5000"));
        var user = new UserAccount("Matheu", "matheu@example.com", "hash", UserAccount.Role.USER, new BigDecimal("995000"));
        when(bets.findByIdForSettlement("b-1")).thenReturn(Optional.of(bet));
        when(users.findById("u-1")).thenReturn(Optional.of(user));
        service.settle("b-1", "u-1", true, new BigDecimal("10000"));
        service.settle("b-1", "u-1", true, new BigDecimal("10000"));

        assertThat(bet.view().status()).isEqualTo("WON");
        assertThat(bet.view().payout()).isEqualByComparingTo("10000");
        assertThat(user.balance()).isEqualByComparingTo("1005000");
    }

    @Test
    void marcaLaApuestaPerdidaSinModificarElSaldo() {
        var bet = new BetRequestEntity("b-2", "u-2", "m-2", "Junior", new BigDecimal("5000"));
        when(bets.findByIdForSettlement("b-2")).thenReturn(Optional.of(bet));
        service.settle("b-2", "u-2", false, BigDecimal.ZERO);

        assertThat(bet.view().status()).isEqualTo("LOST");
        assertThat(bet.view().payout()).isEqualByComparingTo("0");
    }

    @Test
    void reembolsaUnaApuestaRechazadaExactamenteUnaVez() {
        var bet = new BetRequestEntity("b-3", "u-3", "m-3", "Cali", new BigDecimal("5000"));
        var user = new UserAccount("Matheu", "matheu@example.com", "hash", UserAccount.Role.USER, new BigDecimal("245000"));
        when(bets.findByIdForSettlement("b-3")).thenReturn(Optional.of(bet));
        when(users.findById("u-3")).thenReturn(Optional.of(user));

        service.reconcile("b-3", "u-3", "REJECTED", BigDecimal.ZERO);
        service.reconcile("b-3", "u-3", "REJECTED", BigDecimal.ZERO);

        assertThat(bet.view().status()).isEqualTo("REJECTED");
        assertThat(user.balance()).isEqualByComparingTo("250000");
        verify(users, times(1)).findById("u-3");
    }
}
