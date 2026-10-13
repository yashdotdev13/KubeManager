package com.kubemanager.auth_service.service;



import com.kubemanager.auth_service.config.JwtProperties;
import com.kubemanager.auth_service.dto.request.LoginRequest;
import com.kubemanager.auth_service.dto.request.LogoutRequest;
import com.kubemanager.auth_service.dto.request.RefreshTokenRequest;
import com.kubemanager.auth_service.dto.request.RegisterRequest;
import com.kubemanager.auth_service.dto.response.AuthenticationResponse;
import com.kubemanager.auth_service.dto.response.UserResponse;
import com.kubemanager.auth_service.entity.RefreshToken;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.exception.InvalidTokenException;
import com.kubemanager.auth_service.mapper.AuthenticationMapper;
import com.kubemanager.auth_service.mapper.UserMapper;

import com.kubemanager.auth_service.service.Impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationMapper authenticationMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthenticationTokenService authenticationTokenService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;
    private UserResponse userResponse;
    private RefreshToken refreshToken;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();

        userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("refresh-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .revoked(false)
                .expired(false)
                .build();

        authenticationResponse = mock(AuthenticationResponse.class);
    }

    @Test
    void register_shouldCreateUserAndReturnAuthenticationResponse() {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .build();

        when(userService.createUser(request)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(authenticationMapper.toResponse(
                userResponse,
                "access-token",
                "refresh-token",
                900L
        )).thenReturn(authenticationResponse);

        AuthenticationResponse result = authenticationService.register(request);

        assertSame(authenticationResponse, result);
        verify(userService).createUser(request);
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
    void login_shouldAuthenticateAndReturnTokens() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("Password123!")
                .build();

        when(userService.findByUsername("testuser"))
                .thenReturn(Optional.of(user));
        when(authenticationTokenService.createAuthenticationResponse(user))
                .thenReturn(authenticationResponse);

        AuthenticationResponse result = authenticationService.login(request);

        assertSame(authenticationResponse, result);
        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ));
        verify(userService).findByUsername("testuser");
        verify(authenticationTokenService).createAuthenticationResponse(user);
    }

    @Test
    void login_shouldFindUserByEmailWhenUsernameLookupFails() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("test@example.com")
                .password("Password123!")
                .build();

        when(userService.findByUsername("test@example.com"))
                .thenReturn(Optional.empty());
        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(authenticationTokenService.createAuthenticationResponse(user))
                .thenReturn(authenticationResponse);

        AuthenticationResponse result = authenticationService.login(request);

        assertSame(authenticationResponse, result);
        verify(userService).findByEmail("test@example.com");
        verify(authenticationTokenService).createAuthenticationResponse(user);
    }

    @Test
    void refreshToken_shouldIssueNewTokens() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("new-refresh-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .revoked(false)
                .expired(false)
                .build();

        when(refreshTokenService.findByToken("refresh-token"))
                .thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newRefreshToken);
        when(userMapper.toResponse(user)).thenReturn(userResponse);
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(authenticationMapper.toResponse(
                userResponse,
                "new-access-token",
                "new-refresh-token",
                900L
        )).thenReturn(authenticationResponse);

        AuthenticationResponse result = authenticationService.refreshToken(request);

        assertSame(authenticationResponse, result);
        verify(refreshTokenService).verifyExpiration(refreshToken);
        verify(jwtService).generateAccessToken(user);
        verify(refreshTokenService).deleteByUser(user);
        verify(refreshTokenService).createRefreshToken(user);
    }

    @Test
    void refreshToken_shouldRejectUnknownRefreshToken() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("unknown-token")
                .build();

        when(refreshTokenService.findByToken("unknown-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> authenticationService.refreshToken(request)
        );

        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    void logout_shouldDeleteRefreshTokenForUser() {
        LogoutRequest request = LogoutRequest.builder()
                .refreshToken("refresh-token")
                .build();

        when(refreshTokenService.findByToken("refresh-token"))
                .thenReturn(Optional.of(refreshToken));

        assertDoesNotThrow(() -> authenticationService.logout(request));

        verify(refreshTokenService).deleteByUser(user);
    }

    @Test
    void logout_shouldRejectUnknownRefreshToken() {
        LogoutRequest request = LogoutRequest.builder()
                .refreshToken("unknown-token")
                .build();

        when(refreshTokenService.findByToken("unknown-token"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidTokenException.class,
                () -> authenticationService.logout(request)
        );

        verify(refreshTokenService, never()).deleteByUser(any());
    }
}
