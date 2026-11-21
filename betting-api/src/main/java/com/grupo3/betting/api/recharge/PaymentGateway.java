package com.grupo3.betting.api.recharge;

import java.math.BigDecimal;

interface PaymentGateway {
    PaymentOrder createOrder(String reference, String idempotencyKey, BigDecimal amount, String payerEmail);
    PaymentOrder getOrder(String orderId);
}

record PaymentOrder(String id, String externalReference, BigDecimal amount, BigDecimal paidAmount,
                    String currency, String status, String statusDetail, String checkoutUrl) {
    boolean accredited() {
        return "processed".equalsIgnoreCase(status) && "accredited".equalsIgnoreCase(statusDetail);
    }
    boolean finalFailure() {
        return "failed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)
                || "canceled".equalsIgnoreCase(status) || "expired".equalsIgnoreCase(status);
    }
}
