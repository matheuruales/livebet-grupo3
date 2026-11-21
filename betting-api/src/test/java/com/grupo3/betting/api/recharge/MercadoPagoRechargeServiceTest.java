package com.grupo3.betting.api.recharge;

import com.grupo3.betting.api.account.UserAccount;
import com.grupo3.betting.api.account.UserAccountRepository;
import com.grupo3.betting.api.notification.AccountEmailNotifier;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MercadoPagoRechargeServiceTest {
    @Test
    void creaUnaOrdenPendienteSinAcreditarSaldoAntesDelPago() {
        var users = mock(UserAccountRepository.class);
        var recharges = mock(PaymentRechargeRepository.class);
        var gateway = mock(PaymentGateway.class);
        var user = user();
        when(users.findById(user.id())).thenReturn(Optional.of(user));
        when(recharges.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        when(gateway.createOrder(anyString(), anyString(), eq(new BigDecimal("50000.00")), eq(user.email())))
                .thenAnswer(call -> new PaymentOrder("ORD-1", call.getArgument(0), new BigDecimal("50000.00"),
                        BigDecimal.ZERO, "COP", "created", "created", "https://mercadopago.test/checkout"));
        var service = service(users, recharges, gateway, mock(AccountEmailNotifier.class));

        var result = service.createCheckout(user.id(), new CheckoutRequest(new BigDecimal("50000")));

        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.checkoutUrl()).isEqualTo("https://mercadopago.test/checkout");
        assertThat(user.balance()).isEqualByComparingTo("250000");
    }

    @Test
    void acreditaUnaSolaVezAunqueElWebhookSeRepita() {
        var users = mock(UserAccountRepository.class);
        var recharges = mock(PaymentRechargeRepository.class);
        var gateway = mock(PaymentGateway.class);
        var emails = mock(AccountEmailNotifier.class);
        var user = user();
        var recharge = new PaymentRecharge(user.id(), new BigDecimal("50000"));
        recharge.attach(new PaymentOrder("ORD-1", recharge.reference(), recharge.amount(), BigDecimal.ZERO,
                "COP", "created", "created", "https://mercadopago.test/checkout"));
        var approved = new PaymentOrder("ORD-1", recharge.reference(), recharge.amount(), recharge.amount(),
                "COP", "processed", "accredited", null);
        when(gateway.getOrder("ORD-1")).thenReturn(approved);
        when(recharges.findByReferenceForUpdate(recharge.reference())).thenReturn(Optional.of(recharge));
        when(users.findByIdForUpdate(user.id())).thenReturn(Optional.of(user));
        var service = service(users, recharges, gateway, emails);

        service.processOrder("ORD-1");
        service.processOrder("ORD-1");

        assertThat(user.balance()).isEqualByComparingTo("300000");
        assertThat(recharge.status()).isEqualTo(PaymentRecharge.Status.APPROVED);
        verify(emails, times(1)).send(eq("notify.email.recharge"), any());
        verify(users, times(1)).findByIdForUpdate(user.id());
    }

    @Test
    void rechazaUnaConfirmacionConMontoAlterado() {
        var users = mock(UserAccountRepository.class);
        var recharges = mock(PaymentRechargeRepository.class);
        var gateway = mock(PaymentGateway.class);
        var user = user();
        var recharge = new PaymentRecharge(user.id(), new BigDecimal("50000"));
        recharge.attach(new PaymentOrder("ORD-1", recharge.reference(), recharge.amount(), BigDecimal.ZERO,
                "COP", "created", "created", "https://mercadopago.test/checkout"));
        when(gateway.getOrder("ORD-1")).thenReturn(new PaymentOrder("ORD-1", recharge.reference(),
                new BigDecimal("1000"), new BigDecimal("1000"), "COP", "processed", "accredited", null));
        when(recharges.findByReferenceForUpdate(recharge.reference())).thenReturn(Optional.of(recharge));
        var service = service(users, recharges, gateway, mock(AccountEmailNotifier.class));

        assertThatThrownBy(() -> service.processOrder("ORD-1"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("monto");
        assertThat(user.balance()).isEqualByComparingTo("250000");
        verify(users, never()).findByIdForUpdate(anyString());
    }

    private static MercadoPagoRechargeService service(UserAccountRepository users,
                                                       PaymentRechargeRepository recharges,
                                                       PaymentGateway gateway,
                                                       AccountEmailNotifier emails) {
        return new MercadoPagoRechargeService(users, recharges, gateway, readyProperties(), emails);
    }

    private static MercadoPagoProperties readyProperties() {
        var properties = new MercadoPagoProperties();
        properties.setEnabled(true);
        properties.setAccessToken("APP_USR-test");
        properties.setWebhookSecret("secret");
        properties.setSuccessUrl("https://livebet.test/?payment=success");
        properties.setPendingUrl("https://livebet.test/?payment=pending");
        properties.setFailureUrl("https://livebet.test/?payment=failure");
        return properties;
    }

    private static UserAccount user() {
        return new UserAccount("Matheu", "matheu@example.com", "hash", UserAccount.Role.USER,
                new BigDecimal("250000"));
    }
}
