package com.grupo3.betting.email;

import com.grupo3.betting.contracts.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "livebet.mail.enabled", havingValue = "true")
class EmailNotificationWorker {
    private static final Logger log= LoggerFactory.getLogger(EmailNotificationWorker.class);
    private final String instance;
    private final JavaMailSender mail;
    private final EmailTemplateRenderer template;
    private final String fromAddress;
    private final String fromName;

    EmailNotificationWorker(@Value("${HOSTNAME:local-worker}") String instance, JavaMailSender mail,
                            EmailTemplateRenderer template,
                            @Value("${livebet.mail.from-address}") String fromAddress,
                            @Value("${livebet.mail.from-name:LiveBet}") String fromName) {
        this.instance = instance;
        this.mail = mail;
        this.template = template;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
    }

    @RabbitListener(queues="email_notifications", concurrency="1")
    void send(EmailNotification notification) throws Exception {
        if (notification.recipientEmail() == null || notification.recipientEmail().isBlank()) {
            throw new IllegalArgumentException("La notificación no contiene un correo de destino");
        }
        var message = mail.createMimeMessage();
        var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(fromAddress, fromName);
        helper.setTo(notification.recipientEmail());
        helper.setSubject(template.subject(notification));
        helper.setText(template.plainText(notification), template.html(notification));
        message.setHeader("X-LiveBet-Notification-ID", notification.notificationId());
        mail.send(message);
        log.info("[{}] Correo {} enviado a {} para la apuesta {}", instance, notification.outcome(),
                notification.recipientEmail(), notification.betId());
    }
}
