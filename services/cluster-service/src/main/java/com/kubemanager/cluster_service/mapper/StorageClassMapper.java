package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.StorageClassResponse;
import com.kubemanager.cluster_service.dto.response.StorageClassSummaryResponse;
import io.fabric8.kubernetes.api.model.storage.StorageClass;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class StorageClassMapper {

    public StorageClassResponse toResponse(
            StorageClass storageClass
    ) {

        OffsetDateTime creationTimestamp = null;

        if (storageClass.getMetadata() != null &&
                storageClass.getMetadata().getCreationTimestamp() != null) {

            creationTimestamp = OffsetDateTime.parse(
                    storageClass.getMetadata()
                            .getCreationTimestamp()
            );
        }

        return StorageClassResponse.builder()
                .name(
                        storageClass.getMetadata().getName()
                )
                .provisioner(
                        storageClass.getProvisioner()
                )
                .reclaimPolicy(
                        storageClass.getReclaimPolicy()
                )
                .volumeBindingMode(
                        storageClass.getVolumeBindingMode()
                )
                .allowVolumeExpansion(
                        storageClass.getAllowVolumeExpansion()
                )
                .parameters(
                        storageClass.getParameters()
                )
                .creationTimestamp(
                        creationTimestamp
                )
                .build();
    }

    public StorageClassSummaryResponse toSummaryResponse(
            StorageClass storageClass
    ) {

        return StorageClassSummaryResponse.builder()
                .name(
                        storageClass.getMetadata().getName()
                )
                .provisioner(
                        storageClass.getProvisioner()
                )
                .reclaimPolicy(
                        storageClass.getReclaimPolicy()
                )
                .volumeBindingMode(
                        storageClass.getVolumeBindingMode()
                )
                .allowVolumeExpansion(
                        storageClass.getAllowVolumeExpansion()
                )
                .build();
    }
}