package com.kubemanager.auth_service.service;

import com.kubemanager.auth_service.entity.RefreshToken;
import com.kubemanager.auth_service.entity.User;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    RefreshToken verifyExpiration(RefreshToken token);

    void revoke(RefreshToken token);

    void deleteByUser(User user);

}
