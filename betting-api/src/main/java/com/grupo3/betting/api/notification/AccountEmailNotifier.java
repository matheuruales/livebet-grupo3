package com.grupo3.betting.api.notification;

import com.grupo3.betting.contracts.notification.EmailNotification;

public interface AccountEmailNotifier {
    void send(String routingKey, EmailNotification notification);
}
