package com.kubemanager.auth_service.service.Impl;

import com.kubemanager.auth_service.config.JwtProperties;
import com.kubemanager.auth_service.dto.request.LoginRequest;
import com.kubemanager.auth_service.dto.request.LogoutRequest;
import com.kubemanager.auth_service.dto.request.RefreshTokenRequest;
import com.kubemanager.auth_service.dto.request.RegisterRequest;
import com.kubemanager.auth_service.dto.response.AuthenticationResponse;
import com.kubemanager.auth_service.dto.response.UserResponse;
import com.kubemanager.auth_service.entity.RefreshToken;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.exception.AuthenticationException;
import com.kubemanager.auth_service.exception.InvalidTokenException;
import com.kubemanager.auth_service.mapper.AuthenticationMapper;
import com.kubemanager.auth_service.mapper.UserMapper;
import com.kubemanager.auth_service.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationMapper authenticationMapper;
    private final UserMapper userMapper;
    private final JwtProperties jwtProperties;


    @Override
    public AuthenticationResponse register(RegisterRequest request) {

        log.info("Registration request received for username '{}'.", request.getUsername());

        User user = userService.createUser(request);

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        UserResponse userResponse = userMapper.toResponse(user);

        AuthenticationResponse response = authenticationMapper.toResponse(
                userResponse,
                accessToken,
                refreshToken.getToken(),
                jwtProperties.getAccessTokenExpirationSeconds()
        );

        log.info("User '{}' registered successfully.", user.getUsername());
        return response;
    }

    @Override
    public AuthenticationResponse login(LoginRequest request) {

        log.info("Login request received for '{}'.", request.getUsernameOrEmail());

        User user = userService.findByUsername(request.getUsernameOrEmail())
                .or(() -> userService.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> {

                    log.warn("Login failed. User '{}' not found.",
                            request.getUsernameOrEmail());

                    return new AuthenticationException(
                            "Invalid username/email or password."
                    );
                });

        if (!passwordService.matches(
                request.getPassword(),
                user.getPassword())) {

            log.warn("Invalid password for user '{}'.",
                    request.getUsernameOrEmail());

            throw new AuthenticationException(
                    "Invalid username/email or password."
            );
        }

        refreshTokenService.deleteByUser(user);

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        UserResponse userResponse =
                userMapper.toResponse(user);

        log.info("User '{}' logged in successfully.",
                user.getUsername());

        return authenticationMapper.toResponse(
                userResponse,
                accessToken,
                refreshToken.getToken(),
                jwtProperties.getAccessTokenExpirationSeconds()
        );
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {

        log.info("Refresh token request received.");

        RefreshToken refreshToken = refreshTokenService
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> {

                    log.warn("Refresh token not found.");

                    return new InvalidTokenException(
                            "Invalid refresh token."
                    );
                });

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        String accessToken = jwtService.generateAccessToken(user);

        refreshTokenService.deleteByUser(user);

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        UserResponse userResponse = userMapper.toResponse(user);

        log.info("Access token refreshed successfully for user '{}'.",
                user.getUsername());

        return authenticationMapper.toResponse(
                userResponse,
                accessToken,
                newRefreshToken.getToken(),
                jwtProperties.getAccessTokenExpirationSeconds()
        );
    }

    @Override
    public void logout(LogoutRequest request) {

        log.info("Logout request received.");

        RefreshToken refreshToken = refreshTokenService
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> {

                    log.warn("Refresh token not found during logout.");

                    return new InvalidTokenException(
                            "Invalid refresh token."
                    );
                });

        refreshTokenService.deleteByUser(refreshToken.getUser());

        log.info("User '{}' logged out successfully.",
                refreshToken.getUser().getUsername());
    }
}
