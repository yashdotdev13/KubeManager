package com.kubemanager.auth_service.service;

import com.kubemanager.auth_service.entity.RefreshToken;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.exception.TokenExpiredException;
import com.kubemanager.auth_service.repository.RefreshTokenRepository;

import com.kubemanager.auth_service.service.Impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();

        // @Value is not injected by Mockito, so set it explicitly.
        ReflectionTestUtils.setField(
                refreshTokenService,
                "refreshTokenExpiration",
                3600L
        );
    }

    @Test
    void createRefreshToken_shouldCreateTokenSuccessfully() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertDoesNotThrow(() -> UUID.fromString(result.getToken()));
        assertEquals(user, result.getUser());
        assertFalse(result.getRevoked());
        assertFalse(result.getExpired());
        assertTrue(result.getExpiresAt().isAfter(LocalDateTime.now()));

        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_shouldDeletePreviousTokenBeforeSaving() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        refreshTokenService.createRefreshToken(user);

        var inOrder = inOrder(refreshTokenRepository);
        inOrder.verify(refreshTokenRepository).deleteByUser(user);
        inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void findByToken_shouldReturnTokenWhenFound() {
        RefreshToken token = createToken(
                "test-refresh-token",
                LocalDateTime.now().plusHours(1)
        );

        when(refreshTokenRepository.findByToken("test-refresh-token"))
                .thenReturn(Optional.of(token));

        Optional<RefreshToken> result =
                refreshTokenService.findByToken("test-refresh-token");

        assertTrue(result.isPresent());
        assertSame(token, result.get());
    }

    @Test
    void findByToken_shouldReturnEmptyWhenNotFound() {
        when(refreshTokenRepository.findByToken("unknown-token"))
                .thenReturn(Optional.empty());

        Optional<RefreshToken> result =
                refreshTokenService.findByToken("unknown-token");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUser_shouldReturnTokenWhenFound() {
        RefreshToken token = createToken(
                "test-refresh-token",
                LocalDateTime.now().plusHours(1)
        );

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Optional.of(token));

        Optional<RefreshToken> result =
                refreshTokenService.findByUser(user);

        assertTrue(result.isPresent());
        assertSame(token, result.get());
    }

    @Test
    void verifyExpiration_shouldReturnUnexpiredToken() {
        RefreshToken token = createToken(
                "valid-token",
                LocalDateTime.now().plusHours(1)
        );

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertSame(token, result);
        assertFalse(result.getExpired());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_shouldMarkAndRejectExpiredToken() {
        RefreshToken token = createToken(
                "expired-token",
                LocalDateTime.now().minusMinutes(1)
        );

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                TokenExpiredException.class,
                () -> refreshTokenService.verifyExpiration(token)
        );

        assertTrue(token.getExpired());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void revoke_shouldMarkTokenRevokedAndSaveIt() {
        RefreshToken token = createToken(
                "token-to-revoke",
                LocalDateTime.now().plusHours(1)
        );

        refreshTokenService.revoke(token);

        assertTrue(token.getRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void deleteByUser_shouldDeleteUsersRefreshToken() {
        refreshTokenService.deleteByUser(user);

        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void save_shouldDelegateToRepository() {
        RefreshToken token = createToken(
                "token-to-save",
                LocalDateTime.now().plusHours(1)
        );

        when(refreshTokenRepository.save(token)).thenReturn(token);

        RefreshToken result = refreshTokenService.save(token);

        assertSame(token, result);
        verify(refreshTokenRepository).save(token);
    }

    private RefreshToken createToken(
            String value,
            LocalDateTime expiresAt
    ) {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(value)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .expired(false)
                .build();
    }
}
