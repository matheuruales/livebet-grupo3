package com.grupo3.betting.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class TokenServiceTest {
    @Test void firmaYVerificaUnaSesionSinExponerLaContrasena() {
        var service = new TokenService("secreto-de-prueba-livebet-muy-seguro", 12, new ObjectMapper());
        var user = new UserAccount("Admin", "admin@livebet.co", "hash-privado", UserAccount.Role.ADMIN, BigDecimal.TEN);
        String token = service.issue(user);
        AuthPrincipal principal = service.verify(token);
        assertThat(principal.userId()).isEqualTo(user.id());
        assertThat(principal.role()).isEqualTo("ADMIN");
        assertThat(token).doesNotContain("hash-privado");
    }

    @Test void rechazaUnTokenAlterado() {
        var service = new TokenService("secreto-de-prueba-livebet-muy-seguro", 12, new ObjectMapper());
        assertThatThrownBy(() -> service.verify("abc.def.ghi")).isInstanceOf(IllegalArgumentException.class);
    }
}
