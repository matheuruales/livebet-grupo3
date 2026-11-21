package com.grupo3.betting.api.roulette;

import com.grupo3.betting.api.auth.AuthPrincipal;

import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roulette")
@CrossOrigin(origins = "*")
class RouletteController {
    private final RouletteService service;
    RouletteController(RouletteService service) { this.service = service; }

    @GetMapping("/round") RouletteRoundView current() { return service.current(); }
    @PostMapping("/bets") RouletteBetResponse place(@Valid @RequestBody RouletteBetRequest request) { return service.place(userId(), request); }
    @GetMapping("/bets") List<RouletteBetView> mine() { return service.mine(userId()); }

    private String userId() {
        return ((AuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).userId();
    }
}
