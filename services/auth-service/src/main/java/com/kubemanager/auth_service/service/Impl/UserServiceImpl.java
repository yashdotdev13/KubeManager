package com.kubemanager.auth_service.service.Impl;

import com.kubemanager.auth_service.dto.request.RegisterRequest;
import com.kubemanager.auth_service.dto.response.UserResponse;
import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.mapper.UserMapper;
import com.kubemanager.auth_service.repository.UserRepository;
import com.kubemanager.auth_service.service.PasswordService;
import com.kubemanager.auth_service.service.RoleService;
import com.kubemanager.auth_service.service.UserService;
import com.kubemanager.exception.ConflictException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordService passwordService;
    private final UserMapper userMapper;


    @Override
    public User createUser(RegisterRequest request) {

        log.info("Creating user with username: {}",request.getUsername());

        if(existsByUsername(request.getUsername())){
            log.warn("Username '{}' already exists",request.getUsername());

            throw new ConflictException(
                    ErrorCode.USERNAME_ALREADY_EXISTS,
                    "Username already exists."
            );
        }

        if (existsByEmail(request.getEmail())) {
            log.warn("Email '{}' already exists.", request.getEmail());

            throw new ConflictException(
                    ErrorCode.EMAIL_ALREADY_EXISTS,
                    "Email already exists."
            );
        }

        Role role = roleService.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> {
                    log.error("Default role '{}' not found.", DEFAULT_ROLE);

                    return new ResourceNotFoundException(
                            ErrorCode.ROLE_NOT_FOUND,
                            "Default role not found."
                    );
                });

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordService.encode(request.getPassword()))
                .build();

        user.getRoles().add(role);

        User savedUser = userRepository.save(user);

        log.info("User created successfully with id: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    public User save(User user) {

        log.debug("Saving user '{}'",user.getUsername());
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserResponse getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "user not found."
                ));

        return userMapper.toResponse(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
