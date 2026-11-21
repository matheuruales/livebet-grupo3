package com.grupo3.betting.contracts.notification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DashboardNotification(String matchId, String status, List<String> teams,
                                    String winner, Integer homeScore, Integer awayScore,
                                    int bets, BigDecimal totalBet, Instant startsAt,
                                    Instant endsAt, Instant occurredAt) {}
