package com.grupo3.betting.dashboard.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DashboardRabbitConfig {
    @Bean TopicExchange notificationsExchange(){return new TopicExchange("notifications",true,false);}
    @Bean Queue dashboardQueue(){return QueueBuilder.durable("dashboard_notifications").build();}
    @Bean Binding dashboardBinding(){return BindingBuilder.bind(dashboardQueue()).to(notificationsExchange()).with("notify.dashboard.*");}
    @Bean Jackson2JsonMessageConverter rabbitJsonConverter(){return new Jackson2JsonMessageConverter();}
}
