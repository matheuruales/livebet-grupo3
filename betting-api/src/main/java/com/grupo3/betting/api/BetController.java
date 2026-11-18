package com.grupo3.betting.api;

import com.grupo3.betting.contracts.BetPlacedEvent;
import com.grupo3.betting.domain.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bets")
@CrossOrigin(origins = "*")
class BetController {
    private final BetPublisher publisher;
    BetController(BetPublisher publisher) { this.publisher = publisher; }
    @PostMapping ResponseEntity<Map<String,Object>> place(@Valid @RequestBody PlaceBetRequest request) {
        var userId = new UserId(request.userId()); var matchId = new MatchId(request.matchId()); new Money(request.amount());
        String betId = "b-" + UUID.randomUUID().toString().substring(0, 8);
        var event = new BetPlacedEvent(UUID.randomUUID().toString(), betId, userId.value(), matchId.value(),
                request.betOn(), request.amount(), Instant.now());
        publisher.publish(event);
        return ResponseEntity.accepted().body(Map.of("status", "ACCEPTED_FOR_PROCESSING", "betId", betId,
                "message", "La apuesta será validada por el motor de resultados"));
    }
}
