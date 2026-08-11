package com.kubemanager.cluster_service.service.Impl;


import com.kubemanager.cluster_service.dto.request.CreateStorageClassRequest;
import com.kubemanager.cluster_service.dto.response.StorageClassResponse;
import com.kubemanager.cluster_service.dto.response.StorageClassSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.StorageClassMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.StorageClassService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.storage.StorageClass;
import io.fabric8.kubernetes.api.model.storage.StorageClassBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageClassServiceImpl implements StorageClassService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final StorageClassMapper storageClassMapper;

    @Override
    public StorageClassResponse createStorageClass(
            UUID clusterId,
            CreateStorageClassRequest request
    ) {

        log.info(
                "Creating StorageClass '{}' for cluster '{}'.",
                request.getName(),
                clusterId
        );

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));

        if (cluster.getEncryptedKubeConfig() == null ||
                cluster.getEncryptedKubeConfig().isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster kubeconfig is not available."
            );
        }

        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {

            StorageClass existingStorageClass =
                    client.storage()
                            .v1()
                            .storageClasses()
                            .withName(request.getName())
                            .get();

            if (existingStorageClass != null) {

                throw new BadRequestException(
                        ErrorCode.STORAGE_CLASS_ALREADY_EXISTS,
                        "StorageClass already exists."
                );
            }

            StorageClass storageClass =
                    buildStorageClass(request);

            StorageClass createdStorageClass =
                    client.storage()
                            .v1()
                            .storageClasses()
                            .resource(storageClass)
                            .create();

            log.info(
                    "StorageClass '{}' created successfully.",
                    request.getName()
            );

            return storageClassMapper.toResponse(
                    createdStorageClass
            );

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create StorageClass '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.STORAGE_CLASS_CREATION_FAILED,
                    "Unable to create StorageClass."
            );
        }
    }

    private StorageClass buildStorageClass(
            CreateStorageClassRequest request
    ) {

        StorageClassBuilder builder =
                new StorageClassBuilder()

                        .withNewMetadata()
                        .withName(request.getName())
                        .endMetadata()

                        .withProvisioner(
                                request.getProvisioner()
                        )

                        .withReclaimPolicy(
                                request.getReclaimPolicy() != null
                                        ? request.getReclaimPolicy()
                                        : "Delete"
                        )

                        .withVolumeBindingMode(
                                request.getVolumeBindingMode() != null
                                        ? request.getVolumeBindingMode()
                                        : "Immediate"
                        )

                        .withAllowVolumeExpansion(
                                request.getAllowVolumeExpansion() != null
                                        ? request.getAllowVolumeExpansion()
                                        : false
                        );

        if (request.getParameters() != null &&
                !request.getParameters().isEmpty()) {

            builder.withParameters(
                    request.getParameters()
            );
        }

        return builder.build();
    }

    @Override
    public List<StorageClassSummaryResponse> getStorageClasses(
            UUID clusterId
    ) {

        log.info(
                "Fetching StorageClasses for cluster '{}'.",
                clusterId
        );

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));

        if (cluster.getEncryptedKubeConfig() == null ||
                cluster.getEncryptedKubeConfig().isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster kubeconfig is not available."
            );
        }

        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {

            List<StorageClass> storageClasses =
                    client.storage()
                            .v1()
                            .storageClasses()
                            .list()
                            .getItems();

            log.info(
                    "Found {} StorageClass(es) for cluster '{}'.",
                    storageClasses.size(),
                    clusterId
            );

            return storageClasses.stream()
                    .map(storageClassMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch StorageClasses for cluster '{}'.",
                    clusterId,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.STORAGE_CLASS_NOT_FOUND,
                    "Unable to fetch StorageClasses."
            );
        }
    }

    @Override
    public StorageClassResponse getStorageClass(
            UUID clusterId,
            String storageClassName
    ) {

        log.info(
                "Fetching StorageClass '{}' for cluster '{}'.",
                storageClassName,
                clusterId
        );

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));

        if (cluster.getEncryptedKubeConfig() == null ||
                cluster.getEncryptedKubeConfig().isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster kubeconfig is not available."
            );
        }

        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {

            StorageClass storageClass =
                    client.storage()
                            .v1()
                            .storageClasses()
                            .withName(storageClassName)
                            .get();

            if (storageClass == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.STORAGE_CLASS_NOT_FOUND,
                        "StorageClass not found."
                );
            }

            log.info(
                    "StorageClass '{}' fetched successfully.",
                    storageClassName
            );

            return storageClassMapper.toResponse(storageClass);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch StorageClass '{}'.",
                    storageClassName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.STORAGE_CLASS_NOT_FOUND,
                    "Unable to fetch StorageClass."
            );
        }
    }

    @Override
    public void deleteStorageClass(
            UUID clusterId,
            String storageClassName
    ) {

        log.info(
                "Deleting StorageClass '{}' for cluster '{}'.",
                storageClassName,
                clusterId
        );

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));

        if (cluster.getEncryptedKubeConfig() == null ||
                cluster.getEncryptedKubeConfig().isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster kubeconfig is not available."
            );
        }

        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {

            StorageClass storageClass =
                    client.storage()
                            .v1()
                            .storageClasses()
                            .withName(storageClassName)
                            .get();

            if (storageClass == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.STORAGE_CLASS_NOT_FOUND,
                        "StorageClass not found."
                );
            }

            client.storage()
                    .v1()
                    .storageClasses()
                    .withName(storageClassName)
                    .delete();

            log.info(
                    "StorageClass '{}' deleted successfully.",
                    storageClassName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete StorageClass '{}'.",
                    storageClassName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.STORAGE_CLASS_DELETION_FAILED,
                    "Unable to delete StorageClass."
            );
        }
    }
}