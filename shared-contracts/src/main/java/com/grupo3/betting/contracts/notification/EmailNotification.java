package com.grupo3.betting.contracts.notification;

import java.math.BigDecimal;
import java.time.Instant;

public record EmailNotification(String notificationId, String recipientEmail, String userId, String betId,
                                String matchId, String outcome, String homeTeam, String awayTeam,
                                String selectedTeam, String winner, Integer homeScore, Integer awayScore,
                                BigDecimal stake, BigDecimal payout, Instant occurredAt,
                                String reference, String cardLast4, BigDecimal balance) {
    public EmailNotification(String notificationId, String recipientEmail, String userId, String betId,
                             String matchId, String outcome, String homeTeam, String awayTeam,
                             String selectedTeam, String winner, Integer homeScore, Integer awayScore,
                             BigDecimal stake, BigDecimal payout, Instant occurredAt) {
        this(notificationId, recipientEmail, userId, betId, matchId, outcome, homeTeam, awayTeam,
                selectedTeam, winner, homeScore, awayScore, stake, payout, occurredAt, null, null, null);
    }

    public EmailNotification(String notificationId, String userId, String matchId,
                             String outcome, BigDecimal amount) {
        this(notificationId, null, userId, null, matchId, outcome, null, null,
                null, null, null, null, amount, "WIN".equals(outcome) ? amount : BigDecimal.ZERO,
                Instant.now(), null, null, null);
    }

    public static EmailNotification recharge(String notificationId, String recipientEmail, String userId,
                                             String reference, BigDecimal amount, String cardLast4,
                                             BigDecimal balance, Instant occurredAt) {
        return new EmailNotification(notificationId, recipientEmail, userId, null, null, "RECHARGE",
                null, null, null, null, null, null, amount, BigDecimal.ZERO, occurredAt,
                reference, cardLast4, balance);
    }

    public static EmailNotification roulette(String notificationId, String recipientEmail, String userId,
                                             String betId, String roundId, boolean won, String selection,
                                             int result, String color, BigDecimal amount, BigDecimal payout,
                                             Instant occurredAt) {
        return new EmailNotification(notificationId, recipientEmail, userId, betId, roundId,
                won ? "ROULETTE_WIN" : "ROULETTE_LOSS", "Ruleta LiveBet", null, selection,
                result + " · " + color, result, null, amount, payout, occurredAt, null, null, null);
    }
}
