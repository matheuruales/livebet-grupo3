package com.grupo3.betting.email;

import com.grupo3.betting.contracts.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class EmailNotificationWorker {
    private static final Logger log= LoggerFactory.getLogger(EmailNotificationWorker.class);
    private final String instance;
    EmailNotificationWorker(@Value("${HOSTNAME:local-worker}") String instance){this.instance=instance;}
    @RabbitListener(queues="email_notifications", concurrency="1")
    void send(EmailNotification n){
        log.info("[{}] ENVIANDO EMAIL a {} | partido {} | {}",instance,n.userId(),n.matchId(),message(n));
    }
    String message(EmailNotification n) {
        return switch(n.outcome()){
            case "WIN" -> "¡Ganaste " + n.amount() + "!";
            case "LOSS" -> "La apuesta finalizó sin premio. Valor: " + n.amount();
            default -> "Apuesta rechazada. Valor: " + n.amount();
        };
    }
}
