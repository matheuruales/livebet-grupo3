package com.grupo3.betting.api.recharge;

import com.grupo3.betting.api.account.UserAccount;
import com.grupo3.betting.api.account.UserAccountRepository;
import com.grupo3.betting.api.notification.AccountEmailNotifier;

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
        var emails = mock(AccountEmailNotifier.class);
        var user = new UserAccount("Matheu", "matheu@example.com", "hash", UserAccount.Role.USER, new BigDecimal("250000"));
        when(users.findByIdForUpdate(user.id())).thenReturn(Optional.of(user));
        when(recharges.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var service = new RechargeService(users, recharges, emails);

        var result = service.recharge(user.id(), new RechargeRequest(new BigDecimal("50000"), "Matheu Ruales",
                "4242 4242 4242 4242", "12/30", "123"));

        assertThat(result.status()).isEqualTo("APPROVED");
        assertThat(result.cardLast4()).isEqualTo("4242");
        assertThat(result.balance()).isEqualByComparingTo("300000");
        verify(recharges).save(any(RechargeTransaction.class));
        verify(emails).send(eq("notify.email.recharge"), any());
    }

    @Test
    void rechazaUnaTarjetaInvalidaSinCambiarElSaldo() {
        var users = mock(UserAccountRepository.class);
        var service = new RechargeService(users, mock(RechargeRepository.class), mock(AccountEmailNotifier.class));

        assertThatThrownBy(() -> service.recharge("u-1", new RechargeRequest(new BigDecimal("50000"), "Matheu Ruales",
                "1234 5678 9012 345", "12/30", "123")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("16 cifras");
        verifyNoInteractions(users);
    }

    @Test
    void aceptaCualquierCombinacionDeDieciseisCifras() {
        assertThat(RechargeService.validCard("1111222233334444")).isTrue();
        assertThat(RechargeService.validCard("9876543210123456")).isTrue();
        assertThat(RechargeService.validCard("123456789012345")).isFalse();
    }
}
