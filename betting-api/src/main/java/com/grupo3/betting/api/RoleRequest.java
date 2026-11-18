package com.grupo3.betting.api;

import jakarta.validation.constraints.NotNull;

record RoleRequest(@NotNull UserAccount.Role role) {}
