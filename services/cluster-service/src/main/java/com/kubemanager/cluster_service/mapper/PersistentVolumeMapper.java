package com.kubemanager.cluster_service.mapper;


import com.kubemanager.cluster_service.dto.response.PersistentVolumeResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeSummaryResponse;
import io.fabric8.kubernetes.api.model.PersistentVolume;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class PersistentVolumeMapper {

    public PersistentVolumeResponse toResponse(
            PersistentVolume persistentVolume
    ) {

        String capacity = null;
        String accessMode = null;
        String claim = null;
        String hostPath = null;

        if (persistentVolume.getSpec() != null) {

            if (persistentVolume.getSpec().getCapacity() != null &&
                    persistentVolume.getSpec()
                            .getCapacity()
                            .get("storage") != null) {

                capacity = persistentVolume.getSpec()
                        .getCapacity()
                        .get("storage")
                        .toString();
            }

            if (persistentVolume.getSpec().getAccessModes() != null &&
                    !persistentVolume.getSpec()
                            .getAccessModes()
                            .isEmpty()) {

                accessMode = persistentVolume.getSpec()
                        .getAccessModes()
                        .get(0);
            }

            if (persistentVolume.getSpec().getClaimRef() != null) {

                String namespace =
                        persistentVolume.getSpec()
                                .getClaimRef()
                                .getNamespace();

                String claimName =
                        persistentVolume.getSpec()
                                .getClaimRef()
                                .getName();

                if (namespace != null && claimName != null) {
                    claim = namespace + "/" + claimName;
                } else {
                    claim = claimName;
                }
            }

            if (persistentVolume.getSpec().getHostPath() != null) {

                hostPath = persistentVolume.getSpec()
                        .getHostPath()
                        .getPath();
            }
        }

        OffsetDateTime creationTimestamp = null;

        if (persistentVolume.getMetadata() != null &&
                persistentVolume.getMetadata().getCreationTimestamp() != null) {

            creationTimestamp = OffsetDateTime.parse(
                    persistentVolume.getMetadata()
                            .getCreationTimestamp()
            );
        }

        return PersistentVolumeResponse.builder()
                .name(
                        persistentVolume.getMetadata().getName()
                )
                .status(
                        persistentVolume.getStatus() != null
                                ? persistentVolume.getStatus().getPhase()
                                : null
                )
                .capacity(capacity)
                .storageClassName(
                        persistentVolume.getSpec() != null
                                ? persistentVolume.getSpec()
                                .getStorageClassName()
                                : null
                )
                .accessMode(accessMode)
                .reclaimPolicy(
                        persistentVolume.getSpec() != null
                                ? persistentVolume.getSpec()
                                .getPersistentVolumeReclaimPolicy()
                                : null
                )
                .claim(claim)
                .hostPath(hostPath)
                .creationTimestamp(creationTimestamp)
                .build();
    }

    public PersistentVolumeSummaryResponse toSummaryResponse(
            PersistentVolume persistentVolume
    ) {

        String capacity = null;
        String accessMode = null;
        String claim = null;

        if (persistentVolume.getSpec() != null) {

            if (persistentVolume.getSpec().getCapacity() != null &&
                    persistentVolume.getSpec()
                            .getCapacity()
                            .get("storage") != null) {

                capacity = persistentVolume.getSpec()
                        .getCapacity()
                        .get("storage")
                        .toString();
            }

            if (persistentVolume.getSpec().getAccessModes() != null &&
                    !persistentVolume.getSpec()
                            .getAccessModes()
                            .isEmpty()) {

                accessMode = persistentVolume.getSpec()
                        .getAccessModes()
                        .get(0);
            }

            if (persistentVolume.getSpec().getClaimRef() != null) {

                String namespace =
                        persistentVolume.getSpec()
                                .getClaimRef()
                                .getNamespace();

                String claimName =
                        persistentVolume.getSpec()
                                .getClaimRef()
                                .getName();

                if (namespace != null && claimName != null) {
                    claim = namespace + "/" + claimName;
                } else {
                    claim = claimName;
                }
            }
        }

        return PersistentVolumeSummaryResponse.builder()
                .name(
                        persistentVolume.getMetadata().getName()
                )
                .status(
                        persistentVolume.getStatus() != null
                                ? persistentVolume.getStatus().getPhase()
                                : null
                )
                .capacity(capacity)
                .storageClassName(
                        persistentVolume.getSpec() != null
                                ? persistentVolume.getSpec()
                                .getStorageClassName()
                                : null
                )
                .accessMode(accessMode)
                .claim(claim)
                .build();
    }
}