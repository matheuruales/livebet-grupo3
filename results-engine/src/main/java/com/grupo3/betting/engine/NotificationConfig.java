package com.grupo3.betting.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class NotificationConfig {
    @Bean ObjectMapper objectMapper() { return new ObjectMapper().findAndRegisterModules(); }
    static final String EXCHANGE = "notifications";
    @Bean TopicExchange notificationsExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean Jackson2JsonMessageConverter rabbitJsonConverter() { return new Jackson2JsonMessageConverter(); }
}
