package com.kubemanager.auth_service.service;


import com.kubemanager.auth_service.entity.User;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.UUID;

public interface JwtService {

    String generateAccessToken(User user);

    boolean validateToken(String token);

    UUID extractUserId(String token);

    String extractUsername(String token);

    Date extractExpiration(String token);

    Claims extractAllClaims(String token);

}