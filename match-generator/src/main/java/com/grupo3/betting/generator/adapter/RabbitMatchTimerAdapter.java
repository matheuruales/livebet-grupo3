package com.grupo3.betting.generator.adapter;

import com.grupo3.betting.generator.config.MessagingConfig;
import com.grupo3.betting.generator.port.MatchTimerPort;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.command.MatchSimulationCommand;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
class RabbitMatchTimerAdapter implements MatchTimerPort {
    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;
    RabbitMatchTimerAdapter(RabbitTemplate rabbit, ObjectMapper mapper) { this.rabbit = rabbit; this.mapper = mapper; }
    @Override public void schedule(MatchSimulationCommand command, String messageId, long delayMs) {
        try {
            var properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setHeader("x-delay", delayMs);
            properties.setMessageId(messageId);
            rabbit.send(MessagingConfig.DELAYED_EXCHANGE, "match.simulate", new Message(mapper.writeValueAsBytes(command), properties));
        } catch (Exception exception) { throw new IllegalStateException("No se pudo programar el partido", exception); }
    }
}
