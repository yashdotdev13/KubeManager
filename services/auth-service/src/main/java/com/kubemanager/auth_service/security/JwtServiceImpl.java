package com.kubemanager.auth_service.security;


import com.kubemanager.auth_service.config.JwtProperties;
import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.exception.InvalidTokenException;
import com.kubemanager.auth_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {


    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    public void initialize() {

        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );

        log.info("JWT Secret Key initialized successfully.");
    }

    @Override
    public String generateAccessToken(User user) {

        log.info("Generating access token for user '{}'", user.getUsername());

        Date now = new Date();

        Date expiryDate = new Date(
                now.getTime() + jwtProperties.getAccessTokenExpiration() * 1000
        );

        return Jwts.builder()
                .claims(buildClaims(user))
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }



    @Override
    public boolean validateToken(String token) {

        try {

            Claims claims = extractAllClaims(token);

            boolean valid = claims.getExpiration().after(new Date());

            if (!valid) {

                log.warn("JWT token has expired.");
                return false;
            }
            return true;

        } catch (InvalidTokenException exception) {
            log.warn("JWT validation failed: {}", exception.getMessage());

            return false;
        } catch (Exception exception) {

            log.error("Unexpected error while validating JWT.", exception);
            return false;
        }
    }

    @Override
    public UUID extractUserId(String token) {

        try {

            String userId = extractAllClaims(token)
                    .get("userId", String.class);

            return UUID.fromString(userId);

        } catch (Exception exception) {
            log.error("Failed to extract user id from JWT.", exception);

            throw new InvalidTokenException(
                    "Unable to extract user id from JWT."
            );
        }
    }

    @Override
    public String extractUsername(String token) {

        try {

            return extractAllClaims(token).getSubject();
        } catch (Exception exception) {

            log.error("Failed to extract username from JWT.", exception);

            throw new InvalidTokenException(
                    "Unable to extract username from JWT."
            );
        }
    }

    @Override
    public Date extractExpiration(String token) {

        return extractAllClaims(token)
                .getExpiration();
    }

    @Override
    public Claims extractAllClaims(String token) {

        try {

            return getClaims(token);

        } catch (Exception exception) {

            log.error("Failed to extract JWT claims.", exception);

            throw new InvalidTokenException(
                    "Invalid JWT token."
            );
        }
    }


    private SecretKey getSigningKey() {
        return secretKey;
    }

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    private Map<String, Object> buildClaims(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put(
                "roles",
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .toList()
        );
        return claims;
    }
}
