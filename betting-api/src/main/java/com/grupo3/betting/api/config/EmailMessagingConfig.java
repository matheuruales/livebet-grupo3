package com.grupo3.betting.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailMessagingConfig {
    public static final String EXCHANGE = "notifications";

    @Bean
    TopicExchange accountNotificationsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Jackson2JsonMessageConverter accountRabbitJsonConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }
}
