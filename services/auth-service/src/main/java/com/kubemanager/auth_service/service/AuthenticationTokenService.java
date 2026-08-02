package com.kubemanager.auth_service.service;

import com.kubemanager.auth_service.dto.response.AuthenticationResponse;
import com.kubemanager.auth_service.entity.User;

public interface AuthenticationTokenService {

    AuthenticationResponse createAuthenticationResponse(User user);

}