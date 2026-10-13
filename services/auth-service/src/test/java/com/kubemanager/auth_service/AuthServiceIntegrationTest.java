package com.kubemanager.auth_service;

import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.enums.AuthProvider;
import com.kubemanager.auth_service.repository.RoleRepository;
import com.kubemanager.auth_service.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.defer-datasource-initialization=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
@ActiveProfiles("test")
@Testcontainers
class AuthServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );
    }

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void springContextStartsSuccessfully() {
        assertTrue(postgres.isRunning());
    }

    @Test
    void shouldPersistAndRetrieveRole() {
        Role role = Role.builder()
                .name("ROLE_INTEGRATION_TEST")
                .build();

        Role savedRole = roleRepository.saveAndFlush(role);

        assertNotNull(savedRole.getId());
        assertEquals("ROLE_INTEGRATION_TEST", savedRole.getName());

        Optional<Role> retrievedRole =
                roleRepository.findByName("ROLE_INTEGRATION_TEST");

        assertTrue(retrievedRole.isPresent());
        assertEquals(savedRole.getId(), retrievedRole.get().getId());
        assertEquals(
                "ROLE_INTEGRATION_TEST",
                retrievedRole.get().getName()
        );
    }

    @Test
    void shouldReturnEmptyWhenRoleDoesNotExist() {
        Optional<Role> result =
                roleRepository.findByName("ROLE_DOES_NOT_EXIST");

        assertTrue(result.isEmpty());
    }


    @Test
    void shouldPersistAndRetrieveUserById() {
        User user = User.builder()
                .username("integration_user")
                .email("integration@example.com")
                .password("encoded-test-password")
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());

        User retrievedUser = userRepository.findById(savedUser.getId())
                .orElseThrow();

        assertEquals("integration_user", retrievedUser.getUsername());
        assertEquals("integration@example.com", retrievedUser.getEmail());
        assertEquals(AuthProvider.LOCAL, retrievedUser.getProvider());
    }

    @Test
    void shouldPersistAndRetrieveUserRoles() {
        Role role = roleRepository.saveAndFlush(
                Role.builder()
                        .name("ROLE_INTEGRATION_USER")
                        .build()
        );

        User user = User.builder()
                .username("role_test_user")
                .email("role-test@example.com")
                .password("encoded-test-password")
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(role);

        User savedUser = userRepository.saveAndFlush(user);
        UUID userId = savedUser.getId();

        // Clear the persistence context so the next read comes from the database.
        entityManager.clear();

        User retrievedUser = userRepository.findById(userId)
                .orElseThrow();

        assertEquals(1, retrievedUser.getRoles().size());
        assertTrue(
                retrievedUser.getRoles().stream()
                        .anyMatch(r -> "ROLE_INTEGRATION_USER".equals(r.getName()))
        );
    }

    @Test
    void shouldRejectDuplicateUsername() {
        User firstUser = User.builder()
                .username("duplicate_user")
                .email("first@example.com")
                .password("encoded-test-password")
                .provider(AuthProvider.LOCAL)
                .roles(new HashSet<>())
                .build();

        userRepository.saveAndFlush(firstUser);

        User secondUser = User.builder()
                .username("duplicate_user")
                .email("second@example.com")
                .password("encoded-test-password")
                .provider(AuthProvider.LOCAL)
                .roles(new HashSet<>())
                .build();

        assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(secondUser)
        );
    }
}
