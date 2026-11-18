package com.grupo3.betting.api;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RechargeServiceTest {
    @Test
    void apruebaUnaTarjetaFicticiaYAbonaElSaldo() {
        var users = mock(UserAccountRepository.class);
        var recharges = mock(RechargeRepository.class);
        var user = new UserAccount("Matheu", "matheu@example.com", "hash", UserAccount.Role.USER, new BigDecimal("250000"));
        when(users.findByIdForUpdate(user.id())).thenReturn(Optional.of(user));
        when(recharges.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new RechargeService(users, recharges);

        var result = service.recharge(user.id(), new RechargeRequest(new BigDecimal("50000"), "Matheu Ruales",
                "4242 4242 4242 4242", "12/30", "123"));

        assertThat(result.status()).isEqualTo("APPROVED");
        assertThat(result.cardLast4()).isEqualTo("4242");
        assertThat(result.balance()).isEqualByComparingTo("300000");
        verify(recharges).save(any(RechargeTransaction.class));
    }

    @Test
    void rechazaUnaTarjetaInvalidaSinCambiarElSaldo() {
        var users = mock(UserAccountRepository.class);
        var service = new RechargeService(users, mock(RechargeRepository.class));

        assertThatThrownBy(() -> service.recharge("u-1", new RechargeRequest(new BigDecimal("50000"), "Matheu Ruales",
                "4242 4242 4242 4241", "12/30", "123")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("rechazada");
        verifyNoInteractions(users);
    }
}
