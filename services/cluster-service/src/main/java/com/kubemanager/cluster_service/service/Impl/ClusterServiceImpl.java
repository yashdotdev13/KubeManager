package com.kubemanager.cluster_service.service.Impl;


import com.kubemanager.cluster_service.auth.UserContext;
import com.kubemanager.cluster_service.auth.UserContextHolder;
import com.kubemanager.cluster_service.dto.request.CreateClusterRequest;
import com.kubemanager.cluster_service.dto.request.UpdateClusterRequest;
import com.kubemanager.cluster_service.dto.response.ClusterResponse;
import com.kubemanager.cluster_service.dto.response.ClusterSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import com.kubemanager.cluster_service.kubernates.service.KubernetesConnectionService;
import com.kubemanager.cluster_service.mapper.ClusterMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.ClusterService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ConflictException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ClusterServiceImpl implements ClusterService {

    private final ClusterRepository clusterRepository;
    private final ClusterMapper clusterMapper;
    private final KubernetesConnectionService kubernetesConnectionService;


    @Override
    public ClusterResponse createCluster(CreateClusterRequest request) {


        if(request== null){
            throw new BadRequestException(ErrorCode.INVALID_CLUSTER,
                    "Request body cannot be null.");
        }

        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID ownerId = userContext.getUserId();

        log.info("Creating cluster '{}' for user '{}' ({})",
                request.getName(),
                userContext.getUsername(),
                ownerId);

        if(clusterRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, request.getName())){

            log.warn("Cluster '{}' already exists for user '{}'",
                    request.getName(),
                    userContext.getUsername());

            throw new ConflictException(
                    ErrorCode.CLUSTER_ALREADY_EXISTS,
                    "Cluster with the same name already exists."
            );
        }

        Cluster cluster = clusterMapper.toEntity(request, ownerId);

        Cluster savedCluster = clusterRepository.save(cluster);

        log.info("Cluster '{}' created successfully.",savedCluster.getName());
        return clusterMapper.toResponse(savedCluster);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ClusterSummaryResponse> getMyClusters() {

        UserContext userContext = UserContextHolder.getRequiredContext();

        log.info(
                "Fetching all clusters for user '{}'",
                userContext.getUsername()
        );

        List<Cluster> clusters =
                clusterRepository.findAllByOwnerId(
                        userContext.getUserId()
                );

        return clusterMapper.toSummaryResponseList(clusters);
    }

    @Override
    @Transactional(readOnly = true)
    public ClusterResponse getClusterById(
            UUID clusterId
    ) {

        UserContext userContext = UserContextHolder.getRequiredContext();

        Cluster cluster = clusterRepository
                .findByIdAndOwnerId(
                        clusterId,
                        userContext.getUserId()
                )
                .orElseThrow(() -> {

                    log.warn(
                            "Cluster '{}' not found.",
                            clusterId
                    );

                    return new ResourceNotFoundException(
                            ErrorCode.CLUSTER_NOT_FOUND,
                            "Cluster not found."
                    );
                });

        return clusterMapper.toResponse(cluster);
    }

    @Override
    public ClusterResponse updateCluster(
            UUID clusterId,
            UpdateClusterRequest request
    ) {

        if (request == null) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER,
                    "Request body cannot be null."
            );
        }

        UserContext userContext = UserContextHolder.getRequiredContext();

        Cluster cluster = clusterRepository
                .findByIdAndOwnerId(
                        clusterId,
                        userContext.getUserId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));

        clusterMapper.updateEntity(
                cluster,
                request
        );

        Cluster updated =
                clusterRepository.save(cluster);

        log.info(
                "Cluster '{}' updated successfully.",
                updated.getName()
        );

        return clusterMapper.toResponse(updated);
    }

    @Override
    public void deleteCluster(UUID clusterId) {

        UserContext userContext = UserContextHolder.getRequiredContext();

        Cluster cluster = clusterRepository
                .findByIdAndOwnerId(
                        clusterId,
                        userContext.getUserId()
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));

        clusterRepository.delete(cluster);

        log.info(
                "Cluster '{}' deleted successfully.",
                cluster.getName()
        );
    }

    @Override
    @Transactional
    public ClusterResponse connectCluster(UUID clusterId, MultipartFile kubeConfig) {


        if (kubeConfig == null || kubeConfig.isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "kubeConfig file is required"
            );
        }

        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID ownerId = userContext.getUserId();

        log.info("Connecting cluster '{}' for user '{}' ({})",
                clusterId,
                userContext.getUsername(),
                ownerId);


        Cluster cluster = clusterRepository.findByIdAndOwnerId(clusterId, ownerId)
                .orElseThrow(() -> {

                    log.warn("Cluster '{}' not found for user '{}'", clusterId, ownerId);

                    return new ResourceNotFoundException(ErrorCode.CLUSTER_NOT_FOUND,
                            "Cluster not found.");
                });
        ClusterMetadata metadata =
                kubernetesConnectionService.connect(kubeConfig);

        cluster.setApiServer(
                metadata.getApiServer()
        );

        cluster.setKubernetesVersion(
                metadata.getKubernetesVersion()
        );

        cluster.setPlatform(
                metadata.getPlatform()
        );

        cluster.setNodeCount(
                metadata.getNodeCount()
        );

        cluster.setNamespaceCount(
                metadata.getNamespaceCount()
        );

        cluster.setStatus(
                metadata.getStatus()
        );

        cluster.setLastHealthCheck(
                metadata.getLastHealthCheck()
        );

        Cluster updatedCluster =
                clusterRepository.save(cluster);

        log.info(
                "Cluster '{}' connected successfully.",
                updatedCluster.getId()
        );

        return clusterMapper.toResponse(updatedCluster);
    }
}
