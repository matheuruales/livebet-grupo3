package com.grupo3.betting.dashboard.messaging;

import com.grupo3.betting.dashboard.websocket.DashboardSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grupo3.betting.contracts.notification.DashboardNotification;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DashboardListener {
    private final Map<String,DashboardNotification> state=new ConcurrentHashMap<>();
    private final Set<String> finalized=ConcurrentHashMap.newKeySet();
    private final DashboardSocketHandler sockets; private final ObjectMapper mapper;
    public DashboardListener(DashboardSocketHandler sockets,ObjectMapper mapper){this.sockets=sockets;this.mapper=mapper;}
    @RabbitListener(queues="dashboard_notifications")
    public void update(DashboardNotification notification) throws Exception {
        boolean open = "OPEN_FOR_BETS".equals(notification.status());
        if (open && finalized.contains(notification.matchId())) return;
        if (!open) finalized.add(notification.matchId());
        state.put(notification.matchId(), notification);
        pruneHistory();
        sockets.broadcast(mapper.writeValueAsString(notification));
    }
    public Map<String,DashboardNotification> state(){return Map.copyOf(state);}

    private void pruneHistory() {
        if (state.size() <= 60) return;
        state.values().stream()
                .filter(item -> !"OPEN_FOR_BETS".equals(item.status()))
                .sorted(Comparator.comparing(DashboardNotification::occurredAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .skip(30)
                .map(DashboardNotification::matchId)
                .toList()
                .forEach(state::remove);
    }
}
