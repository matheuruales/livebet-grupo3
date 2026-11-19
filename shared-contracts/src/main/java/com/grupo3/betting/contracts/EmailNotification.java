package com.grupo3.betting.contracts;

import java.math.BigDecimal;
import java.time.Instant;

public record EmailNotification(String notificationId, String recipientEmail, String userId, String betId,
                                String matchId, String outcome, String homeTeam, String awayTeam,
                                String selectedTeam, String winner, Integer homeScore, Integer awayScore,
                                BigDecimal stake, BigDecimal payout, Instant occurredAt) {
    public EmailNotification(String notificationId, String userId, String matchId,
                             String outcome, BigDecimal amount) {
        this(notificationId, null, userId, null, matchId, outcome, null, null,
                null, null, null, null, amount, "WIN".equals(outcome) ? amount : BigDecimal.ZERO, Instant.now());
    }
}
