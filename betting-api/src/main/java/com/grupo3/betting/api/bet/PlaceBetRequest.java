package com.grupo3.betting.api.bet;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

record PlaceBetRequest(@NotBlank String matchId, @NotBlank String betOn,
                       @NotNull @DecimalMin(value="0.01") BigDecimal amount) {}
