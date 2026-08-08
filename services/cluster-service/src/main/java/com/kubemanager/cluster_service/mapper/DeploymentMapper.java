package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.DeploymentSummaryResponse;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.springframework.stereotype.Component;

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
}