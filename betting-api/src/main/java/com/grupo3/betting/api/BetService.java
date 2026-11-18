package com.grupo3.betting.api;

import com.grupo3.betting.contracts.BetPlacedEvent;
import com.grupo3.betting.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
class BetService {
    private final UserAccountRepository users; private final BetRequestRepository bets; private final BetPublisher publisher;
    BetService(UserAccountRepository users, BetRequestRepository bets, BetPublisher publisher) {
        this.users = users; this.bets = bets; this.publisher = publisher;
    }
    @Transactional Map<String,Object> place(String userId, PlaceBetRequest request) {
        var user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        var matchId = new MatchId(request.matchId()); new Money(request.amount());
        if (request.betOn().isBlank()) throw new IllegalArgumentException("El equipo es obligatorio");
        user.debit(request.amount());
        String betId = "b-" + UUID.randomUUID().toString().substring(0, 8);
        var event = new BetPlacedEvent(UUID.randomUUID().toString(), betId, user.id(), matchId.value(), request.betOn().trim(), request.amount(), Instant.now());
        publisher.publish(event);
        bets.save(new BetRequestEntity(betId, user.id(), matchId.value(), request.betOn().trim(), request.amount()));
        return Map.of("status", "ACCEPTED_FOR_PROCESSING", "betId", betId, "balance", user.balance(),
                "message", "Apuesta registrada y enviada al motor de resultados");
    }
    @Transactional(readOnly = true) List<BetRequestEntity.BetView> mine(String userId) {
        return bets.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream().map(BetRequestEntity::view).toList();
    }
}
