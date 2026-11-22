package com.grupo3.betting.api.recharge;

import com.mercadopago.exceptions.MPInvalidWebhookSignatureException;
import com.mercadopago.webhook.WebhookSignatureValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/recharges/mercadopago")
class MercadoPagoWebhookController {
    private final MercadoPagoRechargeService service;
    private final MercadoPagoProperties properties;

    MercadoPagoWebhookController(MercadoPagoRechargeService service, MercadoPagoProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @PostMapping("/webhook")
    ResponseEntity<Void> webhook(@RequestParam(name = "data.id") String orderId,
                                 @RequestParam(name = "type", required = false) String type,
                                 @RequestHeader(name = "x-signature") String signature,
                                 @RequestHeader(name = "x-request-id") String requestId) {
        properties.requireWebhookReady();
        if (type != null && !"order".equalsIgnoreCase(type)) return ResponseEntity.ok().build();
        try {
            WebhookSignatureValidator.validate(signature, requestId, orderId,
                    properties.webhookSecret(), Duration.ofMinutes(5));
        } catch (MPInvalidWebhookSignatureException exception) {
            return ResponseEntity.status(401).build();
        }
        service.processOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
