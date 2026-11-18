package com.grupo3.betting.api;

import jakarta.validation.constraints.*;

record RegisterRequest(@NotBlank @Size(min=3,max=80) String name,
                       @NotBlank @Email String email,
                       @NotBlank @Size(min=8,max=72) String password) {}
