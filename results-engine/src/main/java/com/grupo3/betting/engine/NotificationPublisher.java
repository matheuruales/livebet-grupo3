package com.grupo3.betting.engine;

import com.grupo3.betting.contracts.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
class NotificationPublisher implements NotificationPort {
    private final RabbitTemplate rabbit;
    NotificationPublisher(RabbitTemplate rabbit) { this.rabbit = rabbit; }
    @Override public void dashboard(String routingKey, DashboardNotification notification) { rabbit.convertAndSend(NotificationConfig.EXCHANGE, routingKey, notification); }
    @Override public void email(String routingKey, EmailNotification notification) { rabbit.convertAndSend(NotificationConfig.EXCHANGE, routingKey, notification); }
}
