package com.grupo3.betting.api;

import com.grupo3.betting.contracts.EmailNotification;

interface AccountEmailNotifier {
    void send(String routingKey, EmailNotification notification);
}
