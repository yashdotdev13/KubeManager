package com.kubemanager.auth_service.repository;

import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface  UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(
            AuthProvider provider,
            String providerId
    );
}
