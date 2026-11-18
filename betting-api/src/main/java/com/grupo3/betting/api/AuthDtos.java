package com.grupo3.betting.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

record RegisterRequest(@NotBlank @Size(min=3,max=80) String name,
                       @NotBlank @Email String email,
                       @NotBlank @Size(min=8,max=72) String password) {}
record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
record AuthResponse(String token, UserView user) {}
record UserView(String id, String name, String email, String role, BigDecimal balance, boolean active, Instant createdAt) {
    static UserView from(UserAccount user) {
        return new UserView(user.id(), user.name(), user.email(), user.role().name(), user.balance(), user.active(), user.createdAt());
    }
}
record BalanceRequest(@NotNull @DecimalMin("1.00") BigDecimal amount) {}
record RoleRequest(@NotNull UserAccount.Role role) {}
record ActiveRequest(boolean active) {}
