package com.kubemanager.auth_service.service;


import com.kubemanager.auth_service.config.JwtProperties;
import com.kubemanager.auth_service.dto.response.AuthenticationResponse;
import com.kubemanager.auth_service.dto.response.UserResponse;
import com.kubemanager.auth_service.entity.RefreshToken;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.mapper.AuthenticationMapper;
import com.kubemanager.auth_service.mapper.UserMapper;

import com.kubemanager.auth_service.service.Impl.AuthenticationTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationTokenServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthenticationTokenServiceImpl authenticationTokenService;

    private User user;
    private RefreshToken refreshToken;
    private UserResponse userResponse;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();

        refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("refresh-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .revoked(false)
                .expired(false)
                .build();

        userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        authenticationResponse = mock(AuthenticationResponse.class);
    }

    @Test
    void createAuthenticationResponse_shouldReturnAuthenticationResponse() {
        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(refreshToken);
        when(userMapper.toResponse(user))
                .thenReturn(userResponse);
        when(jwtProperties.getAccessTokenExpirationSeconds())
                .thenReturn(900L);
        when(authenticationMapper.toResponse(
                userResponse,
                "access-token",
                "refresh-token",
                900L
        )).thenReturn(authenticationResponse);

        AuthenticationResponse result =
                authenticationTokenService.createAuthenticationResponse(user);

        assertSame(authenticationResponse, result);

        verify(refreshTokenService).deleteByUser(user);
        verify(jwtService).generateAccessToken(user);
        verify(refreshTokenService).createRefreshToken(user);
        verify(userMapper).toResponse(user);
        verify(authenticationMapper).toResponse(
                userResponse,
                "access-token",
                "refresh-token",
                900L
        );
    }

    @Test
    void createAuthenticationResponse_shouldDeleteOldRefreshTokenBeforeCreatingNewOne() {
        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(refreshToken);
        when(userMapper.toResponse(user))
                .thenReturn(userResponse);
        when(jwtProperties.getAccessTokenExpirationSeconds())
                .thenReturn(900L);
        when(authenticationMapper.toResponse(
                userResponse,
                "access-token",
                "refresh-token",
                900L
        )).thenReturn(authenticationResponse);

        authenticationTokenService.createAuthenticationResponse(user);

        var inOrder = inOrder(refreshTokenService);
        inOrder.verify(refreshTokenService).deleteByUser(user);
        inOrder.verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    void createAuthenticationResponse_shouldUseConfiguredAccessTokenExpiration() {
        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(refreshToken);
        when(userMapper.toResponse(user))
                .thenReturn(userResponse);
        when(jwtProperties.getAccessTokenExpirationSeconds())
                .thenReturn(1800L);
        when(authenticationMapper.toResponse(
                userResponse,
                "access-token",
                "refresh-token",
                1800L
        )).thenReturn(authenticationResponse);

        authenticationTokenService.createAuthenticationResponse(user);

        verify(authenticationMapper).toResponse(
                userResponse,
                "access-token",
                "refresh-token",
                1800L
        );
    }
}