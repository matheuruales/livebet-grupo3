package com.grupo3.betting.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DashboardNotification(String matchId, String status, List<String> teams,
                                    String winner, int bets, BigDecimal totalBet, Instant occurredAt) {}
