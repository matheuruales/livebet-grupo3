package com.grupo3.betting.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

record BalanceRequest(@NotNull @DecimalMin("1.00") BigDecimal amount) {}
