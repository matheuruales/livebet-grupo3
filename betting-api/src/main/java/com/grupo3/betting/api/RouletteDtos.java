package com.grupo3.betting.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

record RouletteBetRequest(
        @NotBlank String roundId,
        @NotBlank String selectionType,
        @NotBlank String selectionValue,
        @NotNull @DecimalMin("1000") @DecimalMax("500000") BigDecimal amount) {}

record RouletteBetView(String betId, String roundId, String selection, BigDecimal amount,
                       BigDecimal payout, String status, Instant createdAt) {}

record RouletteRoundView(String roundId, String status, Instant serverTime, Instant openedAt,
                         Instant closesAt, Instant resolvesAt, Instant nextRoundAt,
                         Integer result, String resultColor, long totalBets,
                         List<Integer> recentResults) {}

record RouletteBetResponse(String betId, String roundId, String selection, BigDecimal amount,
                           int multiplier, BigDecimal balance) {}
