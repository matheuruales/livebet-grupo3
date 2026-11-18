package com.grupo3.betting.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) {
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    public Money {
        if (amount == null || amount.signum() < 0)
            throw new IllegalArgumentException("El dinero no puede ser negativo");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
    public Money multiply(BigDecimal factor) { return new Money(amount.multiply(factor)); }
}
