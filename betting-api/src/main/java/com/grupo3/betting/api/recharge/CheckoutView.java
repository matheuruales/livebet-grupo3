package com.grupo3.betting.api.recharge;

import java.math.BigDecimal;
import java.time.Instant;

record CheckoutView(String reference, String orderId, BigDecimal amount, String status,
                    String checkoutUrl, Instant createdAt, Instant processedAt, BigDecimal balance) {}

record PaymentConfigurationView(boolean enabled, String provider) {}
