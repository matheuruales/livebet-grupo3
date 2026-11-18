package com.grupo3.betting.api;

import java.time.Instant;
import java.util.List;

record RouletteRoundView(String roundId, String status, Instant serverTime, Instant openedAt,
                         Instant closesAt, Instant resolvesAt, Instant nextRoundAt,
                         Integer result, String resultColor, long totalBets, List<Integer> recentResults) {}
