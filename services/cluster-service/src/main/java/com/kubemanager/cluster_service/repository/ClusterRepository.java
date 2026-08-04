package com.kubemanager.cluster_service.repository;

import com.kubemanager.cluster_service.entity.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClusterRepository extends JpaRepository<Cluster, UUID> {

    List<Cluster> findAllByOwnerId(UUID ownerId);

    Optional<Cluster> findByIdAndOwnerId(
            UUID id,
            UUID ownerId
    );

    boolean existsByOwnerIdAndNameIgnoreCase(
            UUID ownerId,
            String name
    );
}
