package com.grupo3.betting.api.auth;

import jakarta.validation.constraints.*;

record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
