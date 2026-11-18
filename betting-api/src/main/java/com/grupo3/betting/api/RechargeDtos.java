package com.grupo3.betting.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

record RechargeRequest(
        @NotNull @DecimalMin("5000") @DecimalMax("2000000") BigDecimal amount,
        @NotBlank @Size(min=3, max=80) String cardholder,
        @NotBlank @Pattern(regexp="[0-9 ]{13,23}", message="El número de tarjeta no es válido") String cardNumber,
        @NotBlank @Pattern(regexp="(0[1-9]|1[0-2])/[0-9]{2}", message="Usa el formato MM/AA") String expiry,
        @NotBlank @Pattern(regexp="[0-9]{3,4}", message="El código de seguridad no es válido") String cvv) {}

record RechargeView(String reference, BigDecimal amount, String cardLast4, String status,
                    Instant processedAt, BigDecimal balance) {}
