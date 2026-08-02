package com.kubemanager.user_service.repository;


import com.kubemanager.user_service.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferenceRepository
        extends JpaRepository<UserPreference, UUID> {

    Optional<UserPreference> findByUserId(UUID userId);

}