package com.kubemanager.auth_service.mapper;

import com.kubemanager.auth_service.dto.response.AuthenticationResponse;
import com.kubemanager.auth_service.dto.response.TokenResponse;
import com.kubemanager.auth_service.dto.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationMapper {

    public AuthenticationResponse toResponse(
            UserResponse user,
            String accessToken,
            String refreshToken
    ) {

        return AuthenticationResponse.builder()
                .user(user)
                .token(
                        TokenResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .build()
                )
                .build();
    }

}