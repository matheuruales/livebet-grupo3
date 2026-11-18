package com.grupo3.betting.api;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class UserAccountTest {
    @Test void debitaSaldoYProtegeLaInvarianteFinanciera() {
        var user = new UserAccount("Matheu", "MATHEU@EXAMPLE.COM", "hash", UserAccount.Role.USER, new BigDecimal("250000"));
        user.debit(new BigDecimal("15000"));
        assertThat(user.balance()).isEqualByComparingTo("235000");
        assertThat(user.email()).isEqualTo("matheu@example.com");
        assertThatThrownBy(() -> user.debit(new BigDecimal("300000"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("Saldo insuficiente");
    }

    @Test void unaCuentaDesactivadaNoPuedeApostar() {
        var user = new UserAccount("Luis", "luis@example.com", "hash", UserAccount.Role.USER, new BigDecimal("10000"));
        user.setActive(false);
        assertThatThrownBy(() -> user.debit(new BigDecimal("1000"))).isInstanceOf(IllegalStateException.class).hasMessageContaining("desactivada");
    }
}
