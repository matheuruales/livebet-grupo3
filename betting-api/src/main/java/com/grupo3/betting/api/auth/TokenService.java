package com.grupo3.betting.api.auth;

import com.grupo3.betting.api.account.UserAccount;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
class TokenService {
    private final byte[] secret;
    private final long tokenHours;
    private final ObjectMapper mapper;

    TokenService(@Value("${livebet.jwt-secret}") String secret,
                 @Value("${livebet.token-hours}") long tokenHours, ObjectMapper mapper) {
        if (secret.length() < 24) throw new IllegalArgumentException("JWT_SECRET debe tener al menos 24 caracteres");
        this.secret = secret.getBytes(StandardCharsets.UTF_8); this.tokenHours = tokenHours; this.mapper = mapper;
    }

    String issue(UserAccount user) {
        try {
            String header = encode(mapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
            String payload = encode(mapper.writeValueAsBytes(Map.of("sub", user.id(), "email", user.email(),
                    "role", user.role().name(), "exp", Instant.now().plusSeconds(tokenHours * 3600).getEpochSecond())));
            String content = header + "." + payload;
            return content + "." + encode(sign(content));
        } catch (Exception e) { throw new IllegalStateException("No se pudo crear la sesión", e); }
    }

    AuthPrincipal verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Token inválido");
            String content = parts[0] + "." + parts[1];
            if (!java.security.MessageDigest.isEqual(sign(content), Base64.getUrlDecoder().decode(parts[2])))
                throw new IllegalArgumentException("Firma inválida");
            @SuppressWarnings("unchecked") Map<String,Object> claims = mapper.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
            if (((Number) claims.get("exp")).longValue() < Instant.now().getEpochSecond()) throw new IllegalArgumentException("Sesión vencida");
            return new AuthPrincipal((String) claims.get("sub"), (String) claims.get("email"), (String) claims.get("role"));
        } catch (Exception e) { throw new IllegalArgumentException("Sesión inválida", e); }
    }

    private byte[] sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }
    private String encode(byte[] data) { return Base64.getUrlEncoder().withoutPadding().encodeToString(data); }
}
