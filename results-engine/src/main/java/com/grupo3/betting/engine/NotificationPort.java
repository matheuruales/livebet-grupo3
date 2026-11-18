package com.grupo3.betting.engine;

import com.grupo3.betting.contracts.DashboardNotification;
import com.grupo3.betting.contracts.EmailNotification;

interface NotificationPort {
    void dashboard(String routingKey, DashboardNotification notification);
    void email(String routingKey, EmailNotification notification);
}
