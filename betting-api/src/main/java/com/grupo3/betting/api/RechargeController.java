package com.grupo3.betting.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recharges")
@CrossOrigin(origins = "*")
class RechargeController {
    private final RechargeService service;
    RechargeController(RechargeService service) { this.service = service; }

    @PostMapping RechargeView recharge(@Valid @RequestBody RechargeRequest request) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return service.recharge(((AuthPrincipal) authentication.getPrincipal()).userId(), request);
    }
}
