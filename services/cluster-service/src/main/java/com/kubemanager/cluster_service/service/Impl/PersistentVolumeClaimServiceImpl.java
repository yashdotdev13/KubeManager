package com.kubemanager.cluster_service.service.Impl;

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
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
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
        return List.of();
    }

    @Override
    public PersistentVolumeClaimResponse getPersistentVolumeClaim(
            UUID clusterId,
            String namespace,
            String pvcName
    ) {
        return null;
    }

    @Override
    public void deletePersistentVolumeClaim(
            UUID clusterId,
            String namespace,
            String pvcName
    ) {

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
