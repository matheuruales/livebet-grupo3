package com.grupo3.betting.email.template;

import com.grupo3.betting.contracts.notification.EmailNotification;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateRendererTest {
    private final EmailTemplateRenderer renderer = new EmailTemplateRenderer();

    @Test void rendersCompleteWinningEmail() {
        var notification = notification("WIN", new BigDecimal("10000"));

        assertThat(renderer.subject(notification)).contains("Ganaste");
        assertThat(renderer.plainText(notification)).contains("Cali", "$ 5.000 COP", "$ 10.000 COP");
        assertThat(renderer.html(notification)).contains("APUESTA GANADA", "3 - 1", "b-1", "$ 10.000 COP");
    }

    @Test void rendersLossAndRefundVariants() {
        assertThat(renderer.html(notification("LOSS", BigDecimal.ZERO))).contains("APUESTA FINALIZADA", "Perdida");
        assertThat(renderer.html(notification("REJECTED", new BigDecimal("5000"))))
                .contains("APUESTA REEMBOLSADA", "Monto devuelto");
    }

    @Test void escapesUserControlledText() {
        var notification = new EmailNotification("n-1", "user@example.com", "u-1", "b-1", "m-1", "WIN",
                "<script>", "Nacional", "Cali", "Cali", 3, 1, new BigDecimal("5000"),
                new BigDecimal("10000"), Instant.parse("2026-11-18T20:00:00Z"));
        assertThat(renderer.html(notification)).doesNotContain("<script>").contains("&lt;script&gt;");
    }

    @Test void rendersRechargeReceipt() {
        var receipt = EmailNotification.recharge("n-2", "user@example.com", "u-1", "r-1234",
                new BigDecimal("50000"), "9876", new BigDecimal("300000"),
                Instant.parse("2026-11-18T20:00:00Z"));

        assertThat(renderer.subject(receipt)).contains("Recibo");
        assertThat(renderer.plainText(receipt)).contains("$ 50.000 COP", "9876", "r-1234", "$ 300.000 COP");
        assertThat(renderer.html(receipt)).contains("RECARGA APROBADA", "NUEVO SALDO", "•••• 9876");

        var mercadoPago = EmailNotification.recharge("n-3", "user@example.com", "u-1", "rcg-1234",
                new BigDecimal("50000"), "Mercado Pago", new BigDecimal("300000"),
                Instant.parse("2026-11-18T20:00:00Z"));
        assertThat(renderer.html(mercadoPago)).contains("Medio de pago", "Mercado Pago").doesNotContain("•••• Mercado Pago");
    }

    @Test void rendersRouletteWinAndLoss() {
        var win = EmailNotification.roulette("n-3", "user@example.com", "u-1", "rb-1", "rr-1",
                true, "Rojo", 32, "rojo", new BigDecimal("5000"), new BigDecimal("10000"), Instant.now());
        var loss = EmailNotification.roulette("n-4", "user@example.com", "u-1", "rb-2", "rr-1",
                false, "Negro", 32, "rojo", new BigDecimal("5000"), BigDecimal.ZERO, Instant.now());

        assertThat(renderer.html(win)).contains("JUGADA GANADA", "$ 10.000 COP");
        assertThat(renderer.html(loss)).contains("JUGADA FINALIZADA", "Sin premio");
    }

    private EmailNotification notification(String outcome, BigDecimal payout) {
        return new EmailNotification("n-1", "user@example.com", "u-1", "b-1", "m-1", outcome,
                "Cali", "Nacional", "Cali", "Cali", 3, 1, new BigDecimal("5000"), payout,
                Instant.parse("2026-11-18T20:00:00Z"));
    }
}
