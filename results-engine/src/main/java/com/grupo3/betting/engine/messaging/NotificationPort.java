package com.grupo3.betting.engine.messaging;

import com.grupo3.betting.contracts.notification.DashboardNotification;
import com.grupo3.betting.contracts.notification.EmailNotification;

public interface NotificationPort {
    void dashboard(String routingKey, DashboardNotification notification);
    void email(String routingKey, EmailNotification notification);
}
