package com.grupo3.betting.api.recharge;

import com.grupo3.betting.api.account.UserAccountRepository;
import com.grupo3.betting.api.notification.AccountEmailNotifier;
import com.grupo3.betting.contracts.notification.EmailNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
class MercadoPagoRechargeService {
    private final UserAccountRepository users;
    private final PaymentRechargeRepository recharges;
    private final PaymentGateway gateway;
    private final MercadoPagoProperties properties;
    private final AccountEmailNotifier emails;

    MercadoPagoRechargeService(UserAccountRepository users, PaymentRechargeRepository recharges,
                               PaymentGateway gateway, MercadoPagoProperties properties,
                               AccountEmailNotifier emails) {
        this.users = users;
        this.recharges = recharges;
        this.gateway = gateway;
        this.properties = properties;
        this.emails = emails;
    }

    PaymentConfigurationView configuration() {
        return new PaymentConfigurationView(properties.ready(), "Mercado Pago");
    }

    @Transactional(noRollbackFor = PaymentGatewayUnavailableException.class)
    CheckoutView createCheckout(String userId, CheckoutRequest request) {
        properties.requireCheckoutReady();
        var user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var recharge = recharges.saveAndFlush(new PaymentRecharge(userId, request.amount()));
        try {
            var order = gateway.createOrder(recharge.reference(), recharge.idempotencyKey(), recharge.amount(), user.email());
            recharge.attach(order);
            return view(recharge, user.balance());
        } catch (PaymentGatewayUnavailableException exception) {
            recharge.error();
            throw exception;
        }
    }

    @Transactional
    CheckoutView find(String userId, String reference) {
        var recharge = recharges.findByReferenceAndUserId(reference, userId)
                .orElseThrow(() -> new IllegalArgumentException("Recarga no encontrada"));
        // El retorno del checkout también confirma el estado directamente contra Mercado Pago.
        // Así el saldo no queda pendiente si una notificación externa se retrasa.
        if (recharge.providerOrderId() != null && recharge.status() == PaymentRecharge.Status.PENDING) {
            processOrder(recharge.providerOrderId());
        }
        var user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return view(recharge, user.balance());
    }

    @Transactional
    void processOrder(String orderId) {
        var order = gateway.getOrder(orderId);
        if (order.externalReference() == null || order.externalReference().isBlank()) {
            throw new IllegalArgumentException("La orden no contiene una referencia de LiveBet");
        }
        var recharge = recharges.findByReferenceForUpdate(order.externalReference())
                .orElseThrow(() -> new IllegalArgumentException("La orden no pertenece a una recarga de LiveBet"));
        recharge.bindOrder(order.id());
        if (recharge.status() == PaymentRecharge.Status.APPROVED) return;
        if (!"COP".equalsIgnoreCase(order.currency()) || recharge.amount().compareTo(order.amount()) != 0) {
            throw new IllegalArgumentException("El monto o la moneda de la orden no coincide");
        }
        if (order.accredited()) approve(recharge, order);
        else if (order.finalFailure()) recharge.reject();
    }

    private void approve(PaymentRecharge recharge, PaymentOrder order) {
        if (order.paidAmount().compareTo(recharge.amount()) < 0) {
            throw new IllegalArgumentException("Mercado Pago no confirmó el monto completo");
        }
        var user = users.findByIdForUpdate(recharge.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.credit(recharge.amount());
        recharge.approve();
        emails.send("notify.email.recharge", EmailNotification.recharge(UUID.randomUUID().toString(),
                user.email(), user.id(), recharge.reference(), recharge.amount(), "Mercado Pago",
                user.balance(), recharge.processedAt()));
    }

    private static CheckoutView view(PaymentRecharge recharge, BigDecimal balance) {
        return new CheckoutView(recharge.reference(), recharge.providerOrderId(), recharge.amount(),
                recharge.status().name(), recharge.checkoutUrl(), recharge.createdAt(), recharge.processedAt(), balance);
    }
}
