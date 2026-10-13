package com.kubemanager.auth_service.security;

import com.kubemanager.auth_service.config.JwtProperties;
import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.exception.InvalidTokenException;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private JwtProperties jwtProperties;

    private User user;
    private UUID userId;

    // A 64-character secret suitable for an HS512-strength key.
    private static final String TEST_SECRET =
            "01234567890123456789012345678901" +
                    "01234567890123456789012345678901";

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setAccessTokenExpiration(900);
        jwtProperties.setRefreshTokenExpiration(604800);

        jwtService = new JwtServiceImpl(jwtProperties);
        jwtService.initialize();

        userId = UUID.randomUUID();

        Role role = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();

        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .roles(new java.util.HashSet<>(List.of(role)))
                .build();
    }

    @Test
    void generateAccessToken_shouldGenerateValidToken() {
        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void generateAccessToken_shouldContainExpectedClaims() {
        String token = jwtService.generateAccessToken(user);

        Claims claims = jwtService.extractAllClaims(token);

        assertEquals("testuser", claims.getSubject());
        assertEquals(userId.toString(), claims.get("userId", String.class));
        assertEquals(
                List.of("ROLE_USER"),
                claims.get("roles", List.class)
        );
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void extractUsername_shouldReturnUsername() {
        String token = jwtService.generateAccessToken(user);

        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void extractUserId_shouldReturnUserId() {
        String token = jwtService.generateAccessToken(user);

        assertEquals(userId, jwtService.extractUserId(token));
    }

    @Test
    void extractExpiration_shouldReturnFutureExpiration() {
        String token = jwtService.generateAccessToken(user);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_shouldRejectMalformedToken() {
        assertFalse(jwtService.validateToken("not-a-valid-jwt"));
    }

    @Test
    void extractUsername_shouldThrowForMalformedToken() {
        assertThrows(
                InvalidTokenException.class,
                () -> jwtService.extractUsername("not-a-valid-jwt")
        );
    }

    @Test
    void extractUserId_shouldThrowForMalformedToken() {
        assertThrows(
                InvalidTokenException.class,
                () -> jwtService.extractUserId("not-a-valid-jwt")
        );
    }
}