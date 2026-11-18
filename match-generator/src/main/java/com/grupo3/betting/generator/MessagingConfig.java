package com.grupo3.betting.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MessagingConfig {
    static final String DELAYED_EXCHANGE = "match.delayed";
    static final String SIMULATION_QUEUE = "match_simulation_queue";
    @Bean ObjectMapper objectMapper() { return new ObjectMapper().findAndRegisterModules(); }
    @Bean CustomExchange delayedExchange() { return new CustomExchange(DELAYED_EXCHANGE, "x-delayed-message", true, false, java.util.Map.of("x-delayed-type", "direct")); }
    @Bean Queue simulationQueue() { return QueueBuilder.durable(SIMULATION_QUEUE).deadLetterExchange("match.dlx").deadLetterRoutingKey("match.failed").build(); }
    @Bean Binding simulationBinding() { return BindingBuilder.bind(simulationQueue()).to(delayedExchange()).with("match.simulate").noargs(); }
    @Bean DirectExchange deadLetterExchange() { return new DirectExchange("match.dlx"); }
    @Bean Queue deadLetterQueue() { return QueueBuilder.durable("match_simulation_dlq").build(); }
    @Bean Binding deadLetterBinding() { return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("match.failed"); }
}
