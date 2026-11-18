package com.grupo3.betting.contracts;

import java.math.BigDecimal;

public record EmailNotification(String notificationId, String userId, String matchId,
                                String outcome, BigDecimal amount) {}
