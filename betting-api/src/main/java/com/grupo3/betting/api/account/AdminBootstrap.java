package com.grupo3.betting.api.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
class AdminBootstrap implements CommandLineRunner {
    private final UserAccountRepository users; private final PasswordEncoder passwords; private final String email; private final String password;
    AdminBootstrap(UserAccountRepository users, PasswordEncoder passwords,
                   @Value("${livebet.admin-email}") String email, @Value("${livebet.admin-password}") String password) {
        this.users=users; this.passwords=passwords; this.email=email; this.password=password;
    }
    @Override public void run(String... args) {
        if (!users.existsByEmailIgnoreCase(email)) users.save(new UserAccount("Administrador LiveBet", email,
                passwords.encode(password), UserAccount.Role.ADMIN, new BigDecimal("1000000")));
    }
}
