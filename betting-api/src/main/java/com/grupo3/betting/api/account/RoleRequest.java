package com.grupo3.betting.api.account;

import jakarta.validation.constraints.NotNull;

record RoleRequest(@NotNull UserAccount.Role role) {}
