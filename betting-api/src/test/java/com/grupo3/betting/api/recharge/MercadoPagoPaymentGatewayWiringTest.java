package com.grupo3.betting.api.recharge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

class MercadoPagoPaymentGatewayWiringTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void springCanCreateTheGatewayWithItsProductionConstructor() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(MercadoPagoPaymentGateway.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import({MercadoPagoProperties.class, MercadoPagoPaymentGateway.class})
    static class TestConfiguration { }
}
