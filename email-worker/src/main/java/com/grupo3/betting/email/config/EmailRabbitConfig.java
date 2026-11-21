package com.grupo3.betting.email.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EmailRabbitConfig {
    @Bean TopicExchange notificationsExchange(){return new TopicExchange("notifications",true,false);}
    @Bean Queue emailQueue(){return QueueBuilder.durable("email_notifications").deadLetterExchange("notifications.dlx").deadLetterRoutingKey("email.failed").build();}
    @Bean Binding emailBinding(){return BindingBuilder.bind(emailQueue()).to(notificationsExchange()).with("notify.email.*");}
    @Bean DirectExchange notificationDlx(){return new DirectExchange("notifications.dlx");}
    @Bean Queue emailDlq(){return QueueBuilder.durable("email_notifications_dlq").build();}
    @Bean Binding emailDlqBinding(){return BindingBuilder.bind(emailDlq()).to(notificationDlx()).with("email.failed");}
    @Bean Jackson2JsonMessageConverter rabbitJsonConverter(){return new Jackson2JsonMessageConverter();}
}
