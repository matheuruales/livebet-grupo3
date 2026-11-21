package com.grupo3.betting.api.notification;

import com.grupo3.betting.api.config.EmailMessagingConfig;

import com.grupo3.betting.contracts.notification.EmailNotification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
class RabbitAccountEmailNotifier implements AccountEmailNotifier {
    private final RabbitTemplate rabbit;

    RabbitAccountEmailNotifier(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public void send(String routingKey, EmailNotification notification) {
        rabbit.convertAndSend(EmailMessagingConfig.EXCHANGE, routingKey, notification);
    }
}
