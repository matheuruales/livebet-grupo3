package com.grupo3.betting.api.account;

import java.math.BigDecimal;
import java.time.Instant;

public record UserView(String id, String name, String email, String role, BigDecimal balance, boolean active, Instant createdAt) {
    public static UserView from(UserAccount user) {
        return new UserView(user.id(), user.name(), user.email(), user.role().name(), user.balance(), user.active(), user.createdAt());
    }
}
