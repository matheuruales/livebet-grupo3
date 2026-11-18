package com.grupo3.betting.api;

import java.math.BigDecimal;

record RouletteBetResponse(String betId, String roundId, String selection, BigDecimal amount,
                           int multiplier, BigDecimal balance) {}
