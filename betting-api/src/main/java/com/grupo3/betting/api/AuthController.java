package com.grupo3.betting.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @CrossOrigin(origins = "*")
class AuthController {
    private final AuthService auth;
    AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/register") AuthResponse register(@Valid @RequestBody RegisterRequest request) { return auth.register(request); }
    @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
    @GetMapping("/me") UserView me() { return auth.me(principal().userId()); }
    private AuthPrincipal principal() { return (AuthPrincipal) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal(); }
}
