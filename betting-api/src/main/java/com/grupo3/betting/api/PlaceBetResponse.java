package com.grupo3.betting.api;

import java.math.BigDecimal;

record PlaceBetResponse(String status, String betId, BigDecimal balance, String message) {}
