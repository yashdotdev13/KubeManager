package com.kubemanager.auth_service.service.Impl;


import com.kubemanager.auth_service.entity.RefreshToken;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.exception.TokenExpiredException;
import com.kubemanager.auth_service.repository.RefreshTokenRepository;
import com.kubemanager.auth_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public RefreshToken createRefreshToken(User user) {

        log.info("Creating refresh token for user '{}'", user.getUsername());

        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration))
                .revoked(false)
                .expired(false)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created successfully for user '{}'", user.getUsername());
        return savedToken;
    }


    @Override
    public RefreshToken save(RefreshToken token) {

        log.debug("Saving refresh token for user '{}'",
                token.getUser().getUsername());

        return refreshTokenRepository.save(token);
    }


    @Override
    public Optional<RefreshToken> findByToken(String token) {

        log.debug("Searching refresh token.");
        return refreshTokenRepository.findByToken(token);

    }

    @Override
    public Optional<RefreshToken> findByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {

            log.warn("Refresh token expired for user '{}'",
                    token.getUser().getUsername());

            token.setExpired(true);
            refreshTokenRepository.save(token);

            throw new TokenExpiredException(
                    "Refresh token has expired."
            );
        }
        return token;
    }


    @Override
    public void revoke(RefreshToken token) {

        log.info("Revoking refresh token for user '{}'",
                token.getUser().getUsername());

        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Override
    public void deleteByUser(User user) {

        log.info("Deleting refresh token for user '{}'",
                user.getUsername());
        refreshTokenRepository.deleteByUser(user);
    }
}
