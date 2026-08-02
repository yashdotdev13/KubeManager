package com.kubemanager.auth_service.security.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        log.error(
                "OAuth2 authentication failed: {}",
                exception.getMessage(),
                exception
        );

        ErrorResponse errorResponse =
                ErrorResponse.builder()
                        .errorCode(ErrorCode.UNAUTHORIZED.getCode())
                        .message("OAuth2 authentication failed.")
                        .errors(List.of(exception.getMessage()))
                        .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }

}