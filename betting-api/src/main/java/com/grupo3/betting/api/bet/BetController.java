package com.grupo3.betting.api.bet;

import com.grupo3.betting.api.auth.AuthPrincipal;

import com.grupo3.betting.contracts.event.BetPlacedEvent;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping({"/api/bets", "/bet"})
@CrossOrigin(origins = "*")
class BetController {
    private final BetService service;
    BetController(BetService service) { this.service = service; }
    @PostMapping ResponseEntity<PlaceBetResponse> place(@Valid @RequestBody PlaceBetRequest request) {
        return ResponseEntity.accepted().body(service.place(currentUserId(), request));
    }

    @GetMapping List<BetRequestEntity.BetView> mine() { return service.mine(currentUserId()); }

    private String currentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return ((AuthPrincipal) authentication.getPrincipal()).userId();
    }
}
