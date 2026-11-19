package com.grupo3.betting.email;

import com.grupo3.betting.contracts.EmailNotification;
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

    private EmailNotification notification(String outcome, BigDecimal payout) {
        return new EmailNotification("n-1", "user@example.com", "u-1", "b-1", "m-1", outcome,
                "Cali", "Nacional", "Cali", "Cali", 3, 1, new BigDecimal("5000"), payout,
                Instant.parse("2026-11-18T20:00:00Z"));
    }
}
