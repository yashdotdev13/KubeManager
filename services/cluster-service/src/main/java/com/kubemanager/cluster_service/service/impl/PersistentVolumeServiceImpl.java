package com.kubemanager.cluster_service.service.impl;

import com.kubemanager.cluster_service.dto.request.CreatePersistentVolumeRequest;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.PersistentVolumeMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PersistentVolumeService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import io.fabric8.kubernetes.api.model.PersistentVolumeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class PersistentVolumeServiceImpl
        implements PersistentVolumeService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final PersistentVolumeMapper persistentVolumeMapper;

    @Override
    public PersistentVolumeResponse createPersistentVolume(
            UUID clusterId,
            CreatePersistentVolumeRequest request
    ) {

        log.info(
                "Creating PersistentVolume '{}' for cluster '{}'.",
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

            PersistentVolume existingVolume =
                    client.persistentVolumes()
                            .withName(request.getName())
                            .get();

            if (existingVolume != null) {

                throw new BadRequestException(
                        ErrorCode.PV_ALREADY_EXISTS,
                        "PersistentVolume already exists."
                );
            }

            PersistentVolume persistentVolume =
                    buildPersistentVolume(request);

            PersistentVolume createdVolume =
                    client.persistentVolumes()
                            .resource(persistentVolume)
                            .create();

            log.info(
                    "PersistentVolume '{}' created successfully.",
                    request.getName()
            );

            return persistentVolumeMapper.toResponse(
                    createdVolume
            );

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create PersistentVolume '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.PV_CREATION_FAILED,
                    "Unable to create PersistentVolume."
            );
        }
    }

    private PersistentVolume buildPersistentVolume(
            CreatePersistentVolumeRequest request
    ) {

        return new PersistentVolumeBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .endMetadata()

                .withNewSpec()

                .withCapacity(
                        java.util.Map.of(
                                "storage",
                                new Quantity(
                                        request.getStorageSize() + "Gi"
                                )
                        )
                )

                .withAccessModes(
                        "ReadWriteOnce"
                )

                .withPersistentVolumeReclaimPolicy(
                        "Retain"
                )

                .withStorageClassName(
                        request.getStorageClassName()
                )

                .withNewHostPath()
                .withPath(request.getHostPath())
                .endHostPath()

                .endSpec()

                .build();
    }

    @Override
    public List<PersistentVolumeSummaryResponse> getPersistentVolumes(
            UUID clusterId
    ) {

        log.info(
                "Fetching PersistentVolumes for cluster '{}'.",
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

            List<PersistentVolume> volumes =
                    client.persistentVolumes()
                            .list()
                            .getItems();

            log.info(
                    "Found {} PersistentVolume(s) for cluster '{}'.",
                    volumes.size(),
                    clusterId
            );

            return volumes.stream()
                    .map(persistentVolumeMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch PersistentVolumes for cluster '{}'.",
                    clusterId,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.PV_NOT_FOUND,
                    "Unable to fetch PersistentVolumes."
            );
        }
    }

    @Override
    public PersistentVolumeResponse getPersistentVolume(
            UUID clusterId,
            String volumeName
    ) {

        log.info(
                "Fetching PersistentVolume '{}' for cluster '{}'.",
                volumeName,
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

            PersistentVolume persistentVolume =
                    client.persistentVolumes()
                            .withName(volumeName)
                            .get();

            if (persistentVolume == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.PV_NOT_FOUND,
                        "PersistentVolume not found."
                );
            }

            log.info(
                    "PersistentVolume '{}' fetched successfully.",
                    volumeName
            );

            return persistentVolumeMapper.toResponse(
                    persistentVolume
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch PersistentVolume '{}'.",
                    volumeName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.PV_NOT_FOUND,
                    "Unable to fetch PersistentVolume."
            );
        }
    }

    @Override
    public void deletePersistentVolume(
            UUID clusterId,
            String volumeName
    ) {

        log.info(
                "Deleting PersistentVolume '{}' for cluster '{}'.",
                volumeName,
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

            PersistentVolume persistentVolume =
                    client.persistentVolumes()
                            .withName(volumeName)
                            .get();

            if (persistentVolume == null) {
                throw new ResourceNotFoundException(
                        ErrorCode.PV_NOT_FOUND,
                        "PersistentVolume not found."
                );
            }
            client.persistentVolumes()
                    .withName(volumeName)
                    .delete();

            log.info(
                    "PersistentVolume '{}' deleted successfully.",
                    volumeName
            );

        } catch (ResourceNotFoundException exception) {
            throw exception;
        } catch (Exception exception) {

            log.error(
                    "Failed to delete PersistentVolume '{}'.",
                    volumeName,
                    exception
            );
            throw new BadRequestException(
                    ErrorCode.PV_DELETION_FAILED,
                    "Unable to delete PersistentVolume."
            );
        }
    }
}
