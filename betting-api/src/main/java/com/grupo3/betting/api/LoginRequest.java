package com.grupo3.betting.api;

import jakarta.validation.constraints.*;

record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
