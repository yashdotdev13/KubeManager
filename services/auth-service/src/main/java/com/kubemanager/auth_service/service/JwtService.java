package com.kubemanager.auth_service.service;


import com.kubemanager.auth_service.entity.User;

import java.util.UUID;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    boolean validateToken(String token);

    UUID extractUserId(String token);

    String extractUsername(String token);

}
