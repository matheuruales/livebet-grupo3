package com.grupo3.betting.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SettlementServiceTest {
    @Test
    void acreditaElPremioUnaSolaVezYMarcaLaApuestaGanada() {
        var bets = mock(BetRequestRepository.class);
        var users = mock(UserAccountRepository.class);
        var bet = new BetRequestEntity("b-1", "u-1", "m-1", "Cali", new BigDecimal("5000"));
        var user = new UserAccount("Matheu", "matheu@example.com", "hash", UserAccount.Role.USER, new BigDecimal("995000"));
        when(bets.findByIdForSettlement("b-1")).thenReturn(Optional.of(bet));
        when(users.findById("u-1")).thenReturn(Optional.of(user));
        var service = new SettlementService(bets, users);

        service.settle("b-1", "u-1", true, new BigDecimal("10000"));
        service.settle("b-1", "u-1", true, new BigDecimal("10000"));

        assertThat(bet.view().status()).isEqualTo("WON");
        assertThat(bet.view().payout()).isEqualByComparingTo("10000");
        assertThat(user.balance()).isEqualByComparingTo("1005000");
    }

    @Test
    void marcaLaApuestaPerdidaSinModificarElSaldo() {
        var bets = mock(BetRequestRepository.class);
        var users = mock(UserAccountRepository.class);
        var bet = new BetRequestEntity("b-2", "u-2", "m-2", "Junior", new BigDecimal("5000"));
        when(bets.findByIdForSettlement("b-2")).thenReturn(Optional.of(bet));
        var service = new SettlementService(bets, users);

        service.settle("b-2", "u-2", false, BigDecimal.ZERO);

        assertThat(bet.view().status()).isEqualTo("LOST");
        assertThat(bet.view().payout()).isEqualByComparingTo("0");
    }
}
