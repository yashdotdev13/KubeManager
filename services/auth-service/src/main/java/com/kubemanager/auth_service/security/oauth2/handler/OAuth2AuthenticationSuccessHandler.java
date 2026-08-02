package com.kubemanager.auth_service.security.oauth2.handler;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.auth_service.dto.response.AuthenticationResponse;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.security.oauth2.user.OAuth2UserPrincipal;
import com.kubemanager.auth_service.service.AuthenticationTokenService;
import com.kubemanager.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final AuthenticationTokenService authenticationTokenService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2UserPrincipal principal =
                (OAuth2UserPrincipal) authentication.getPrincipal();

        User user = principal.getUser();

        log.info(
                "OAuth2 authentication successful for '{}'.",
                user.getEmail()
        );

        AuthenticationResponse authenticationResponse =
                authenticationTokenService.createAuthenticationResponse(user);

        ApiResponse<AuthenticationResponse> apiResponse =
                ApiResponse.<AuthenticationResponse>builder()
                        .success(true)
                        .message("Authentication successful.")
                        .data(authenticationResponse)
                        .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(
                response.getOutputStream(),
                apiResponse
        );
    }
}