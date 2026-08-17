package com.kubemanager.cluster_service.service.impl;

import com.kubemanager.cluster_service.dto.request.CreatePersistentVolumeClaimRequest;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.PersistentVolumeClaimMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PersistentVolumeClaimService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistentVolumeClaimServiceImpl
        implements PersistentVolumeClaimService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final PersistentVolumeClaimMapper persistentVolumeClaimMapper;

    @Override
    public PersistentVolumeClaimResponse createPersistentVolumeClaim(
            UUID clusterId,
            CreatePersistentVolumeClaimRequest request
    ) {


        log.info("Creating PVC '{}' in namepace '{}'",request.getName(),
                request.getNamespace());

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

            PersistentVolumeClaim existingPvc =
                    client.persistentVolumeClaims()
                            .inNamespace(request.getNamespace())
                            .withName(request.getName())
                            .get();

            if (existingPvc != null) {

                throw new BadRequestException(
                        ErrorCode.PVC_ALREADY_EXISTS,
                        "PersistentVolumeClaim already exists."
                );
            }

            PersistentVolumeClaim pvc =
                    buildPersistentVolumeClaim(request);

            PersistentVolumeClaim createdPvc =
                    client.persistentVolumeClaims()
                            .inNamespace(request.getNamespace())
                            .resource(pvc)
                            .create();

            log.info(
                    "PVC '{}' created successfully.",
                    request.getName()
            );

            return persistentVolumeClaimMapper.toResponse(
                    createdPvc
            );

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create PVC '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.PVC_CREATION_FAILED,
                    "Unable to create PersistentVolumeClaim."
            );
        }
    }

    @Override
    public List<PersistentVolumeClaimSummaryResponse> getPersistentVolumeClaims(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Fetching PVCs for cluster '{}' and namespace '{}'.",
                clusterId,
                namespace
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

            List<PersistentVolumeClaim> pvcs;

            if (namespace == null || namespace.isBlank()) {

                pvcs = client.persistentVolumeClaims()
                        .inAnyNamespace()
                        .list()
                        .getItems();

            } else {

                pvcs = client.persistentVolumeClaims()
                        .inNamespace(namespace)
                        .list()
                        .getItems();
            }

            log.info(
                    "Found {} PVC(s) for cluster '{}'.",
                    pvcs.size(),
                    clusterId
            );

            return pvcs.stream()
                    .map(persistentVolumeClaimMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch PVCs for cluster '{}'.",
                    clusterId,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.PVC_NOT_FOUND,
                    "Unable to fetch PersistentVolumeClaims."
            );
        }
    }

    @Override
    public PersistentVolumeClaimResponse getPersistentVolumeClaim(
            UUID clusterId,
            String namespace,
            String pvcName
    ) {

        log.info(
                "Fetching PVC '{}' from namespace '{}' for cluster '{}'.",
                pvcName,
                namespace,
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

            PersistentVolumeClaim pvc =
                    client.persistentVolumeClaims()
                            .inNamespace(namespace)
                            .withName(pvcName)
                            .get();

            if (pvc == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.PVC_NOT_FOUND,
                        "PersistentVolumeClaim not found."
                );
            }

            log.info(
                    "PVC '{}' fetched successfully.",
                    pvcName
            );

            return persistentVolumeClaimMapper.toResponse(pvc);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch PVC '{}'.",
                    pvcName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.PVC_NOT_FOUND,
                    "Unable to fetch PersistentVolumeClaim."
            );
        }
    }

    @Override
    public void deletePersistentVolumeClaim(
            UUID clusterId,
            String namespace,
            String pvcName
    ) {

        log.info(
                "Deleting PVC '{}' from namespace '{}' for cluster '{}'.",
                pvcName,
                namespace,
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

            PersistentVolumeClaim pvc =
                    client.persistentVolumeClaims()
                            .inNamespace(namespace)
                            .withName(pvcName)
                            .get();

            if (pvc == null) {
                throw new ResourceNotFoundException(
                        ErrorCode.PVC_NOT_FOUND,
                        "PersistentVolumeClaim not found."
                );
            }

            client.persistentVolumeClaims()
                    .inNamespace(namespace)
                    .withName(pvcName)
                    .delete();

            log.info(
                    "PVC '{}' deleted successfully.",
                    pvcName
            );

        } catch (ResourceNotFoundException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Failed to delete PVC '{}'.",
                    pvcName,
                    exception
            );
        }
    }


    private PersistentVolumeClaim buildPersistentVolumeClaim(
            CreatePersistentVolumeClaimRequest request
    ) {

        return new PersistentVolumeClaimBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(request.getNamespace())
                .endMetadata()

                .withNewSpec()

                .withAccessModes(
                        "ReadWriteOnce"
                )

                .withStorageClassName(
                        request.getStorageClassName()
                )

                .withNewResources()

                .addToRequests(
                        "storage",
                        new Quantity(
                                request.getStorageSize() + "Gi"
                        )
                )

                .endResources()

                .endSpec()

                .build();
    }
}
