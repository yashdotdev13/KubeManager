package com.kubemanager.auth_service.service;


import com.kubemanager.auth_service.dto.request.LoginRequest;
import com.kubemanager.auth_service.dto.request.LogoutRequest;
import com.kubemanager.auth_service.dto.request.RefreshTokenRequest;
import com.kubemanager.auth_service.dto.request.RegisterRequest;
import com.kubemanager.auth_service.dto.response.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

}
