package com.grupo3.betting.api.roulette;

import java.math.BigDecimal;
import java.time.Instant;

record RouletteBetView(String betId, String roundId, String selection, BigDecimal amount,
                       BigDecimal payout, String status, Instant createdAt) {}
