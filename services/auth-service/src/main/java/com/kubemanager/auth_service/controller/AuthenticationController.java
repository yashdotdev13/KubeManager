package com.kubemanager.auth_service.controller;


import com.kubemanager.auth_service.dto.request.LoginRequest;
import com.kubemanager.auth_service.dto.request.LogoutRequest;
import com.kubemanager.auth_service.dto.request.RefreshTokenRequest;
import com.kubemanager.auth_service.dto.request.RegisterRequest;
import com.kubemanager.auth_service.dto.response.AuthenticationResponse;
import com.kubemanager.auth_service.service.AuthenticationService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        log.info("Received registration request for '{}'.",
                request.getUsername());

        AuthenticationResponse response =
                authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<AuthenticationResponse>builder()
                                .success(true)
                                .message("User registered successfully.")
                                .data(response)
                                .build()
                );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        log.info("Received login request.");

        AuthenticationResponse response =
                authenticationService.login(request);

        return ResponseEntity.ok(
                ApiResponse.<AuthenticationResponse>builder()
                        .success(true)
                        .message("Login successful.")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {

        log.info("Received refresh token request.");

        AuthenticationResponse response =
                authenticationService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.<AuthenticationResponse>builder()
                        .success(true)
                        .message("Token refreshed successfully.")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {

        log.info("Received logout request.");

        authenticationService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Logout successful.")
                        .build()
        );
    }

}