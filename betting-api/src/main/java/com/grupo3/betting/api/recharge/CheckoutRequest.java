package com.grupo3.betting.api.recharge;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record CheckoutRequest(
        @NotNull @DecimalMin("5000") @DecimalMax("2000000") @Digits(integer = 7, fraction = 2) BigDecimal amount) {}
