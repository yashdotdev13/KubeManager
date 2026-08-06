package com.kubemanager.user_service.repository;

import com.kubemanager.user_service.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface  UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile>  findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}



