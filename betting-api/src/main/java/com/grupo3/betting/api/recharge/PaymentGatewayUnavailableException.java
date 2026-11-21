package com.grupo3.betting.api.recharge;

public class PaymentGatewayUnavailableException extends RuntimeException {
    PaymentGatewayUnavailableException(String message) { super(message); }
    PaymentGatewayUnavailableException(String message, Throwable cause) { super(message, cause); }
}
