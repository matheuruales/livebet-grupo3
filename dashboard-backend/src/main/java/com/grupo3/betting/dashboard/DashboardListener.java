package com.grupo3.betting.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.DashboardNotification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
class DashboardListener {
    private final Map<String,DashboardNotification> state=new ConcurrentHashMap<>();
    private final DashboardSocketHandler sockets; private final ObjectMapper mapper;
    DashboardListener(DashboardSocketHandler sockets,ObjectMapper mapper){this.sockets=sockets;this.mapper=mapper;}
    @RabbitListener(queues="dashboard_notifications")
    void update(DashboardNotification notification) throws Exception {state.put(notification.matchId(),notification);sockets.broadcast(mapper.writeValueAsString(notification));}
    Map<String,DashboardNotification> state(){return Map.copyOf(state);}
}
