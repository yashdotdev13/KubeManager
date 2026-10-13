package com.kubemanager.auth_service.service;

import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.enums.AuthProvider;
import com.kubemanager.auth_service.repository.RoleRepository;
import com.kubemanager.auth_service.repository.UserRepository;
import com.kubemanager.auth_service.security.oauth2.info.OAuth2UserInfo;
import com.kubemanager.auth_service.service.Impl.OAuthUserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OAuth2UserInfo userInfo;

    @InjectMocks
    private OAuthUserServiceImpl oauthUserService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();
    }

    @Test
    void processOauth2UserInfo_shouldReturnExistingGithubUser() {
        // Arrange
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .provider(AuthProvider.GITHUB)
                .providerId("github-123")
                .build();

        when(userInfo.getProviderId()).thenReturn("github-123");

        when(userRepository.findByProviderAndProviderId(
                AuthProvider.GITHUB,
                "github-123"
        )).thenReturn(Optional.of(existingUser));

        // Act
        User result = oauthUserService.processOauth2UserInfo(
                "github",
                userInfo
        );

        // Assert
        assertSame(existingUser, result);

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any(User.class));
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    void processOauth2UserInfo_shouldLinkExistingAccountByEmail() {
        // Arrange
        User existingUser = User.builder()
                .id(UUID.randomUUID())
                .username("localuser")
                .email("test@example.com")
                .provider(AuthProvider.LOCAL)
                .build();

        when(userInfo.getEmail()).thenReturn("test@example.com");
        when(userInfo.getProviderId()).thenReturn("github-123");
        when(userInfo.getAvatarUrl())
                .thenReturn("https://example.com/avatar.png");

        when(userRepository.findByProviderAndProviderId(
                AuthProvider.GITHUB,
                "github-123"
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(existingUser))
                .thenReturn(existingUser);

        // Act
        User result = oauthUserService.processOauth2UserInfo(
                "github",
                userInfo
        );

        // Assert
        assertSame(existingUser, result);
        assertEquals(AuthProvider.GITHUB, result.getProvider());
        assertEquals("github-123", result.getProviderId());
        assertEquals(
                "https://example.com/avatar.png",
                result.getAvatarUrl()
        );

        verify(userRepository).save(existingUser);
        verify(roleRepository, never()).findByName(any());
    }

    @Test
    void processOauth2UserInfo_shouldCreateNewGithubUser() {
        // Arrange
        when(userInfo.getEmail()).thenReturn("test@example.com");
        when(userInfo.getUsername()).thenReturn("testuser");
        when(userInfo.getProviderId()).thenReturn("github-123");
        when(userInfo.getAvatarUrl())
                .thenReturn("https://example.com/avatar.png");

        when(userRepository.findByProviderAndProviderId(
                AuthProvider.GITHUB,
                "github-123"
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = oauthUserService.processOauth2UserInfo(
                "github",
                userInfo
        );

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(AuthProvider.GITHUB, result.getProvider());
        assertEquals("github-123", result.getProviderId());
        assertEquals(
                "https://example.com/avatar.png",
                result.getAvatarUrl()
        );
        assertTrue(result.isEnabled());

        assertNotNull(result.getRoles());
        assertTrue(
                result.getRoles().stream()
                        .anyMatch(role -> "ROLE_USER".equals(role.getName()))
        );

        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void processOauth2UserInfo_shouldFailWhenUserRoleIsMissing() {
        // Arrange
        when(userInfo.getEmail()).thenReturn("test@example.com");
        when(userInfo.getProviderId()).thenReturn("github-123");

        when(userRepository.findByProviderAndProviderId(
                AuthProvider.GITHUB,
                "github-123"
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                IllegalStateException.class,
                () -> oauthUserService.processOauth2UserInfo(
                        "github",
                        userInfo
                )
        );

        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository, never()).save(any(User.class));
    }
}
