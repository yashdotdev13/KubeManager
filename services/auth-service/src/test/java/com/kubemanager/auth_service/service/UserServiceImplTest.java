package com.kubemanager.auth_service.service;

import com.kubemanager.auth_service.dto.request.RegisterRequest;
import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.mapper.UserMapper;
import com.kubemanager.auth_service.repository.UserRepository;
import com.kubemanager.auth_service.service.Impl.UserServiceImpl;
import com.kubemanager.exception.ConflictException;
import com.kubemanager.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest request;
    private Role userRole;

    @BeforeEach
    void setUp() {
        request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .build();

        userRole = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        when(userRepository.existsByUsername("testuser"))
                .thenReturn(false);
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(roleService.findByName("ROLE_USER"))
                .thenReturn(Optional.of(userRole));
        when(passwordService.encode("Password123!"))
                .thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encoded-password", result.getPassword());
        assertEquals(1, result.getRoles().size());
        assertTrue(
                result.getRoles().stream()
                        .anyMatch(role -> "ROLE_USER".equals(role.getName()))
        );

        verify(passwordService).encode("Password123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldRejectDuplicateUsername() {
        when(userRepository.existsByUsername("testuser"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordService, never()).encode(any());
    }

    @Test
    void createUser_shouldRejectDuplicateEmail() {
        when(userRepository.existsByUsername("testuser"))
                .thenReturn(false);
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordService, never()).encode(any());
    }

    @Test
    void createUser_shouldFailWhenDefaultRoleDoesNotExist() {
        when(userRepository.existsByUsername("testuser"))
                .thenReturn(false);
        when(userRepository.existsByEmail("test@example.com"))
                .thenReturn(false);
        when(roleService.findByName("ROLE_USER"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordService, never()).encode(any());
    }
}
