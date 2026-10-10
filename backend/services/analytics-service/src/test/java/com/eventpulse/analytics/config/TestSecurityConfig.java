package com.eventpulse.analytics.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            String subject = (token != null && token.startsWith("sub:")) ? token.substring(4) : "user-organizer";
            List<String> roles;
            if (token != null && token.contains("admin")) {
                roles = List.of("ADMIN");
            } else if (token != null && token.contains("organizer")) {
                roles = List.of("ORGANIZER");
            } else {
                roles = List.of("CUSTOMER");
            }

            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .subject(subject)
                    .claim("preferred_username", subject)
                    .claim("realm_access", Map.of("roles", roles))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        };
    }
}
