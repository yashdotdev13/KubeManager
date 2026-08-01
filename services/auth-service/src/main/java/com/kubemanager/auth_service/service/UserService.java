package com.kubemanager.auth_service.service;

import com.kubemanager.auth_service.dto.request.RegisterRequest;
import com.kubemanager.auth_service.dto.response.UserResponse;
import com.kubemanager.auth_service.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User createUser(RegisterRequest request);

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    UserResponse getUserById(UUID id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}