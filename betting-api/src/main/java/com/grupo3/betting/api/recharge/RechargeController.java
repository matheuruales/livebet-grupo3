package com.grupo3.betting.api.recharge;

import com.grupo3.betting.api.auth.AuthPrincipal;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recharges")
@CrossOrigin(origins = "*")
class RechargeController {
    private final RechargeService service;
    private final MercadoPagoRechargeService mercadoPago;
    private final boolean simulatedPaymentsEnabled;

    RechargeController(RechargeService service, MercadoPagoRechargeService mercadoPago,
                       @org.springframework.beans.factory.annotation.Value("${livebet.simulated-payments-enabled:false}")
                       boolean simulatedPaymentsEnabled) {
        this.service = service;
        this.mercadoPago = mercadoPago;
        this.simulatedPaymentsEnabled = simulatedPaymentsEnabled;
    }

    @PostMapping RechargeView recharge(@Valid @RequestBody RechargeRequest request) {
        if (!simulatedPaymentsEnabled) {
            throw new PaymentGatewayUnavailableException("Las recargas simuladas están deshabilitadas");
        }
        return service.recharge(userId(), request);
    }

    @GetMapping("/payment-config") PaymentConfigurationView paymentConfiguration() {
        return mercadoPago.configuration();
    }

    @PostMapping("/checkout") CheckoutView checkout(@Valid @RequestBody CheckoutRequest request) {
        return mercadoPago.createCheckout(userId(), request);
    }

    @GetMapping("/checkout/{reference}") CheckoutView checkoutStatus(@PathVariable String reference) {
        return mercadoPago.find(userId(), reference);
    }

    private String userId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return ((AuthPrincipal) authentication.getPrincipal()).userId();
    }
}
