package com.grupo3.betting.dashboard.web;

import com.grupo3.betting.dashboard.messaging.DashboardListener;

import com.grupo3.betting.contracts.notification.DashboardNotification;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@RestController
class StateController {
    private final DashboardListener listener;
    StateController(DashboardListener listener){this.listener=listener;}
    @GetMapping("/api/matches")
    Collection<DashboardNotification> matches(){
        Instant staleBefore = Instant.now().minusSeconds(10);
        return listener.state().values().stream()
                .filter(match -> !"OPEN_FOR_BETS".equals(match.status())
                        || match.endsAt() == null
                        || match.endsAt().isAfter(staleBefore))
                .toList();
    }
}
