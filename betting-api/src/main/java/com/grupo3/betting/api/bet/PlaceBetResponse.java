package com.grupo3.betting.api.bet;

import java.math.BigDecimal;

record PlaceBetResponse(String status, String betId, BigDecimal balance, String message) {}
