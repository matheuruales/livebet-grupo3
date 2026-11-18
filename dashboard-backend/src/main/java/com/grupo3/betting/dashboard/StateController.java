package com.grupo3.betting.dashboard;

import com.grupo3.betting.contracts.DashboardNotification;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;

@RestController
class StateController {
    private final DashboardListener listener;
    StateController(DashboardListener listener){this.listener=listener;}
    @GetMapping("/api/matches") Collection<DashboardNotification> matches(){return listener.state().values();}
}
