package com.kubemanager.api_gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenValidator validator;

    public boolean validate(String token) {
        return validator.isValid(token);
    }

    public JwtClaims extractClaims(String token) {

        Claims claims = validator.getClaims(token);

        return JwtClaims.builder()
                .userId(claims.get(JwtConstants.USER_ID, String.class))
                .username(claims.get(JwtConstants.USERNAME, String.class))
                .email(claims.get(JwtConstants.EMAIL, String.class))
                .roles(claims.get(JwtConstants.ROLES, java.util.List.class))
                .build();
    }
}