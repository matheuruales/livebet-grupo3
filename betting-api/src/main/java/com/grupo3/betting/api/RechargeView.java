package com.grupo3.betting.api;

import java.math.BigDecimal;
import java.time.Instant;

record RechargeView(String reference, BigDecimal amount, String cardLast4, String status,
                    Instant processedAt, BigDecimal balance) {}
