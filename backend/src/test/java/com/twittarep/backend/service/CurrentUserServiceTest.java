package com.twittarep.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @Test
    void shouldExtractUserClaimsAndScopes() {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("auth0|123")
            .claim("name", "User One")
            .claim("nickname", "user1")
            .claim("email", "user@example.com")
            .claim("picture", "https://example.com/avatar.png")
            .claim("scope", "read:profile write:posts")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

        var response = currentUserService.getCurrentUser(jwt);

        assertThat(response.sub()).isEqualTo("auth0|123");
        assertThat(response.scopes()).containsExactly("read:profile", "write:posts");
    }
}
