package com.grupo3.betting.email;

import com.grupo3.betting.contracts.EmailNotification;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationWorkerTest {
    private final EmailNotificationWorker worker = new EmailNotificationWorker("test-worker");

    @Test void formatsWinLossAndRejectionMessages() {
        assertThat(worker.message(notification("WIN"))).contains("Ganaste");
        assertThat(worker.message(notification("LOSS"))).contains("sin premio");
        assertThat(worker.message(notification("REJECTED"))).contains("rechazada");
    }

    private EmailNotification notification(String outcome) {
        return new EmailNotification("n-1", "u-1", "m-1", outcome, new BigDecimal("5000"));
    }
}
