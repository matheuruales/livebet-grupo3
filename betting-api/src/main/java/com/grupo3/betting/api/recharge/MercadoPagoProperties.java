package com.grupo3.betting.api.recharge;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "livebet.mercado-pago")
class MercadoPagoProperties {
    private boolean enabled;
    private String accessToken = "";
    private String webhookSecret = "";
    private String successUrl = "";
    private String pendingUrl = "";
    private String failureUrl = "";

    boolean enabled() { return enabled; }
    String accessToken() { return accessToken; }
    String webhookSecret() { return webhookSecret; }
    String successUrl() { return successUrl; }
    String pendingUrl() { return pendingUrl; }
    String failureUrl() { return failureUrl; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public void setSuccessUrl(String successUrl) { this.successUrl = successUrl; }
    public void setPendingUrl(String pendingUrl) { this.pendingUrl = pendingUrl; }
    public void setFailureUrl(String failureUrl) { this.failureUrl = failureUrl; }

    void requireCheckoutReady() {
        if (!enabled) throw new PaymentGatewayUnavailableException("Mercado Pago todavía no está habilitado");
        if (blank(accessToken)) {
            throw new PaymentGatewayUnavailableException("Falta la credencial privada de Mercado Pago");
        }
        if (!validUrl(successUrl) || !validUrl(pendingUrl) || !validUrl(failureUrl)) {
            throw new PaymentGatewayUnavailableException("Configura las URL de retorno de Mercado Pago");
        }
    }

    void requireWebhookReady() {
        requireCheckoutReady();
        if (blank(webhookSecret)) {
            throw new PaymentGatewayUnavailableException("Falta la firma secreta del webhook de Mercado Pago");
        }
    }

    boolean ready() {
        return enabled && !blank(accessToken)
                && validUrl(successUrl) && validUrl(pendingUrl) && validUrl(failureUrl);
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static boolean validUrl(String value) {
        return !blank(value) && (value.startsWith("https://") || value.startsWith("http://localhost"));
    }
}
