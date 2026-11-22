package com.grupo3.betting.api.recharge;

import com.mercadopago.client.order.*;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
class MercadoPagoPaymentGateway implements PaymentGateway {
    private final MercadoPagoProperties properties;
    private final OrderClient client;

    @Autowired
    MercadoPagoPaymentGateway(MercadoPagoProperties properties) {
        this(properties, new OrderClient());
    }

    MercadoPagoPaymentGateway(MercadoPagoProperties properties, OrderClient client) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public PaymentOrder createOrder(String reference, String idempotencyKey, BigDecimal amount, String payerEmail) {
        properties.requireCheckoutReady();
        String value = money(amount);
        var online = OrderOnlineConfig.builder()
                .successUrl(properties.successUrl())
                .pendingUrl(properties.pendingUrl())
                .failureUrl(properties.failureUrl())
                .autoReturn("all")
                .build();
        var request = OrderCreateRequest.builder()
                .type("online")
                .processingMode("manual")
                .captureMode("automatic_async")
                .totalAmount(value)
                .currency("COP")
                .externalReference(reference)
                .description("Recarga de saldo LiveBet")
                .payer(OrderPayerRequest.builder().email(payerEmail).build())
                .config(OrderConfigRequest.builder().statementDescriptor("LIVEBET").online(online).build())
                .items(List.of(OrderItemRequest.builder()
                        .externalCode(reference)
                        .title("Recarga de saldo LiveBet")
                        .description("Fondos para la cuenta LiveBet")
                        .quantity(1)
                        .unitPrice(value)
                        .build()))
                .build();
        return execute(() -> client.create(request, options(idempotencyKey)));
    }

    @Override
    public PaymentOrder getOrder(String orderId) {
        properties.requireCheckoutReady();
        return execute(() -> client.get(orderId, options(null)));
    }

    private PaymentOrder execute(OrderCall call) {
        try {
            return map(call.run());
        } catch (MPApiException exception) {
            throw new PaymentGatewayUnavailableException("Mercado Pago rechazó la operación (HTTP "
                    + exception.getApiResponse().getStatusCode() + ")", exception);
        } catch (MPException exception) {
            throw new PaymentGatewayUnavailableException("No fue posible comunicarse con Mercado Pago", exception);
        }
    }

    private MPRequestOptions options(String idempotencyKey) {
        var builder = MPRequestOptions.builder()
                .accessToken(properties.accessToken())
                .connectionRequestTimeout(3000)
                .connectionTimeout(3000)
                .socketTimeout(7000);
        if (idempotencyKey != null) builder.customHeaders(Map.of("X-Idempotency-Key", idempotencyKey));
        return builder.build();
    }

    private static PaymentOrder map(Order order) {
        return new PaymentOrder(order.getId(), order.getExternalReference(), decimal(order.getTotalAmount()),
                decimal(order.getTotalPaidAmount()), order.getCurrency(), order.getStatus(),
                order.getStatusDetail(), order.getCheckoutUrl());
    }

    private static BigDecimal decimal(String value) {
        return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value);
    }

    private static String money(BigDecimal amount) { return amount.setScale(2).toPlainString(); }

    @FunctionalInterface
    private interface OrderCall { Order run() throws MPException, MPApiException; }
}
