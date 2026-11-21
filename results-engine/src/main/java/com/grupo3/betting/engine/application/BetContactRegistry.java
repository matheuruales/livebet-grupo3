package com.grupo3.betting.engine.application;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
class BetContactRegistry {
    private final Map<String, String> emailByBet = new ConcurrentHashMap<>();

    void remember(String betId, String email) {
        if (email != null && !email.isBlank()) emailByBet.put(betId, email.trim().toLowerCase());
    }

    String emailFor(String betId) {
        return emailByBet.get(betId);
    }
}
