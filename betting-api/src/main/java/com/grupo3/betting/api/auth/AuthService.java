package com.grupo3.betting.api.auth;

import com.grupo3.betting.api.account.UserAccount;
import com.grupo3.betting.api.account.UserAccountRepository;
import com.grupo3.betting.api.account.UserView;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
class AuthService {
    private final UserAccountRepository users; private final PasswordEncoder passwords; private final TokenService tokens;
    private final BigDecimal initialBalance;
    AuthService(UserAccountRepository users, PasswordEncoder passwords, TokenService tokens,
                @Value("${livebet.initial-balance}") BigDecimal initialBalance) {
        this.users = users; this.passwords = passwords; this.tokens = tokens; this.initialBalance = initialBalance;
    }
    @Transactional AuthResponse register(RegisterRequest request) {
        String email = UserAccount.normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) throw new IllegalStateException("Ya existe una cuenta con este correo");
        var user = users.save(new UserAccount(request.name(), email, passwords.encode(request.password()), UserAccount.Role.USER, initialBalance));
        return new AuthResponse(tokens.issue(user), UserView.from(user));
    }
    @Transactional(readOnly = true) AuthResponse login(LoginRequest request) {
        var user = users.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new IllegalArgumentException("Correo o contraseña incorrectos"));
        if (!passwords.matches(request.password(), user.passwordHash())) throw new IllegalArgumentException("Correo o contraseña incorrectos");
        if (!user.active()) throw new IllegalStateException("Esta cuenta está desactivada");
        return new AuthResponse(tokens.issue(user), UserView.from(user));
    }
    @Transactional(readOnly = true) UserView me(String id) { return UserView.from(require(id)); }
    UserAccount require(String id) { return users.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado")); }
}
