package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.DeploymentResponse;
import com.kubemanager.cluster_service.dto.response.DeploymentSummaryResponse;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class DeploymentMapper {

    public DeploymentSummaryResponse toSummaryResponse(
            Deployment deployment
    ) {

        return DeploymentSummaryResponse.builder()
                .name(
                        deployment.getMetadata().getName()
                )
                .namespace(
                        deployment.getMetadata().getNamespace()
                )
                .replicas(
                        deployment.getSpec().getReplicas()
                )
                .readyReplicas(
                        getReadyReplicas(deployment)
                )
                .availableReplicas(
                        getAvailableReplicas(deployment)
                )
                .strategy(
                        deployment.getSpec()
                                .getStrategy()
                                .getType()
                )
                .build();
    }

    public DeploymentResponse toResponse(
            Deployment deployment
    ) {

        return DeploymentResponse.builder()
                .name(
                        deployment.getMetadata().getName()
                )
                .namespace(
                        deployment.getMetadata().getNamespace()
                )
                .replicas(
                        deployment.getSpec().getReplicas()
                )
                .readyReplicas(
                        getReadyReplicas(deployment)
                )
                .availableReplicas(
                        getAvailableReplicas(deployment)
                )
                .updatedReplicas(
                        getUpdatedReplicas(deployment)
                )
                .unavailableReplicas(
                        getUnavailableReplicas(deployment)
                )
                .strategy(
                        deployment.getSpec()
                                .getStrategy()
                                .getType()
                )
                .creationTimestamp(
                        deployment.getMetadata().getCreationTimestamp() != null
                                ? OffsetDateTime.parse(
                                deployment.getMetadata().getCreationTimestamp()
                        )
                                : null
                )
                .labels(
                        deployment.getMetadata().getLabels()
                )
                .annotations(
                        deployment.getMetadata().getAnnotations()
                )
                .build();
    }

    private Integer getReadyReplicas(
            Deployment deployment
    ) {

        return deployment.getStatus() != null &&
                deployment.getStatus().getReadyReplicas() != null
                ? deployment.getStatus().getReadyReplicas()
                : 0;
    }

    private Integer getAvailableReplicas(
            Deployment deployment
    ) {

        return deployment.getStatus() != null &&
                deployment.getStatus().getAvailableReplicas() != null
                ? deployment.getStatus().getAvailableReplicas()
                : 0;
    }

    private Integer getUpdatedReplicas(
            Deployment deployment
    ) {

        return deployment.getStatus() != null &&
                deployment.getStatus().getUpdatedReplicas() != null
                ? deployment.getStatus().getUpdatedReplicas()
                : 0;
    }

    private Integer getUnavailableReplicas(
            Deployment deployment
    ) {

        return deployment.getStatus() != null &&
                deployment.getStatus().getUnavailableReplicas() != null
                ? deployment.getStatus().getUnavailableReplicas()
                : 0;
    }



}