package com.kubemanager.auth_service.service.Impl;


import com.kubemanager.auth_service.config.JwtProperties;
import com.kubemanager.auth_service.dto.response.AuthenticationResponse;
import com.kubemanager.auth_service.dto.response.UserResponse;
import com.kubemanager.auth_service.entity.RefreshToken;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.mapper.AuthenticationMapper;
import com.kubemanager.auth_service.mapper.UserMapper;
import com.kubemanager.auth_service.service.AuthenticationTokenService;
import com.kubemanager.auth_service.service.JwtService;
import com.kubemanager.auth_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationTokenServiceImpl
        implements AuthenticationTokenService {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final AuthenticationMapper authenticationMapper;
    private final JwtProperties jwtProperties;

    @Override
    public AuthenticationResponse createAuthenticationResponse(User user) {

        log.debug(
                "Generating authentication tokens for '{}'.",
                user.getEmail()
        );
        refreshTokenService.deleteByUser(user);


        String accessToken =
                jwtService.generateAccessToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        UserResponse userResponse =
                userMapper.toResponse(user);

        return authenticationMapper.toResponse(
                userResponse,
                accessToken,
                refreshToken.getToken(),
                jwtProperties.getAccessTokenExpirationSeconds()
        );
    }
}