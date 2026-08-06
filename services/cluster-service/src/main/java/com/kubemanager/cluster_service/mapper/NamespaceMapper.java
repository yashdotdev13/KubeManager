package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.NamespaceResponse;
import com.kubemanager.cluster_service.dto.response.NamespaceSummaryResponse;
import io.fabric8.kubernetes.api.model.Namespace;
import org.springframework.stereotype.Component;

@Component
public class NamespaceMapper {

    public NamespaceSummaryResponse toSummaryResponse(
            Namespace namespace
    ) {

        return NamespaceSummaryResponse.builder()
                .name(namespace.getMetadata().getName())
                .status(
                        namespace.getStatus() != null
                                ? namespace.getStatus().getPhase()
                                : "Unknown"
                )
                .build();
    }


    public NamespaceResponse toResponse(
            Namespace namespace
    ) {

        return NamespaceResponse.builder()
                .name(
                        namespace.getMetadata().getName()
                )
                .status(
                        namespace.getStatus() != null
                                ? namespace.getStatus().getPhase()
                                : "Unknown"
                )
                .createdAt(
                        namespace.getMetadata().getCreationTimestamp() != null
                                ? java.time.OffsetDateTime
                                .parse(namespace.getMetadata().getCreationTimestamp())
                                .toLocalDateTime()
                                : null
                )
                .build();
    }
}
