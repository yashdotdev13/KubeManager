package com.kubemanager.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtTokenValidator {

    @Value("${security.jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public boolean isValid(String token) {

        try {

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);

            System.out.println("================================");
            System.out.println("JWT VALID");
            System.out.println("================================");

            return true;

        } catch (Exception ex) {

            System.out.println("================================");
            System.out.println("JWT VALIDATION FAILED");
            ex.printStackTrace();
            System.out.println("================================");

            return false;
        }
    }

    public Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}