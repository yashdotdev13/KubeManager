package com.kubemanager.cluster_service.mapper;



import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimSummaryResponse;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import org.springframework.stereotype.Component;

@Component
public class PersistentVolumeClaimMapper {

    public PersistentVolumeClaimResponse toResponse(
            PersistentVolumeClaim pvc
    ) {

        String storage = null;

        if (pvc.getSpec() != null &&
                pvc.getSpec().getResources() != null &&
                pvc.getSpec().getResources().getRequests() != null &&
                pvc.getSpec().getResources().getRequests().get("storage") != null) {

            storage = pvc.getSpec()
                    .getResources()
                    .getRequests()
                    .get("storage")
                    .toString();
        }

        String accessMode = null;

        if (pvc.getSpec() != null &&
                pvc.getSpec().getAccessModes() != null &&
                !pvc.getSpec().getAccessModes().isEmpty()) {

            accessMode = pvc.getSpec()
                    .getAccessModes()
                    .get(0);
        }

        return PersistentVolumeClaimResponse.builder()
                .name(pvc.getMetadata().getName())
                .namespace(pvc.getMetadata().getNamespace())
                .status(
                        pvc.getStatus() != null
                                ? pvc.getStatus().getPhase()
                                : null
                )
                .storageClassName(
                        pvc.getSpec() != null
                                ? pvc.getSpec().getStorageClassName()
                                : null
                )
                .storage(storage)
                .volumeName(
                        pvc.getSpec() != null
                                ? pvc.getSpec().getVolumeName()
                                : null
                )
                .accessMode(accessMode)
                .creationTimestamp(
                        pvc.getMetadata().getCreationTimestamp() != null
                                ? java.time.OffsetDateTime.parse(
                                pvc.getMetadata().getCreationTimestamp()
                        )
                                : null
                )
                .build();
    }

    public PersistentVolumeClaimSummaryResponse toSummaryResponse(
            PersistentVolumeClaim pvc
    ) {

        String storage = null;

        if (pvc.getSpec() != null &&
                pvc.getSpec().getResources() != null &&
                pvc.getSpec().getResources().getRequests() != null &&
                pvc.getSpec().getResources().getRequests().get("storage") != null) {

            storage = pvc.getSpec()
                    .getResources()
                    .getRequests()
                    .get("storage")
                    .toString();
        }

        return PersistentVolumeClaimSummaryResponse.builder()
                .name(pvc.getMetadata().getName())
                .namespace(pvc.getMetadata().getNamespace())
                .status(
                        pvc.getStatus() != null
                                ? pvc.getStatus().getPhase()
                                : null
                )
                .storageClassName(
                        pvc.getSpec() != null
                                ? pvc.getSpec().getStorageClassName()
                                : null
                )
                .storage(storage)
                .build();
    }
}