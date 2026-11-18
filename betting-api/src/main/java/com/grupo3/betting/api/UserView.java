package com.grupo3.betting.api;

import java.math.BigDecimal;
import java.time.Instant;

record UserView(String id, String name, String email, String role, BigDecimal balance, boolean active, Instant createdAt) {
    static UserView from(UserAccount user) {
        return new UserView(user.id(), user.name(), user.email(), user.role().name(), user.balance(), user.active(), user.createdAt());
    }
}
