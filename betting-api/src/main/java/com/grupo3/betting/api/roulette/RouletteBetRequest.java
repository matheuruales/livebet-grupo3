package com.grupo3.betting.api.roulette;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

record RouletteBetRequest(@NotBlank String roundId, @NotBlank String selectionType,
                          @NotBlank String selectionValue,
                          @NotNull @DecimalMin("1000") @DecimalMax("500000") BigDecimal amount) {}
