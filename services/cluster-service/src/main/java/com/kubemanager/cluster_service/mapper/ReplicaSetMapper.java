package com.kubemanager.cluster_service.mapper;


import com.kubemanager.cluster_service.dto.request.ReplicaSetResponse;
import com.kubemanager.cluster_service.dto.request.ReplicaSetSummaryResponse;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReplicaSetMapper {

    public ReplicaSetResponse toResponse(ReplicaSet replicaSet) {

        String containerName = null;
        String image = null;
        Map<String, String> environment = null;

        if (replicaSet.getSpec() != null &&
                replicaSet.getSpec().getTemplate() != null &&
                replicaSet.getSpec().getTemplate().getSpec() != null &&
                replicaSet.getSpec()
                        .getTemplate()
                        .getSpec()
                        .getContainers() != null &&
                !replicaSet.getSpec()
                        .getTemplate()
                        .getSpec()
                        .getContainers()
                        .isEmpty()) {

            Container container =
                    replicaSet.getSpec()
                            .getTemplate()
                            .getSpec()
                            .getContainers()
                            .get(0);

            containerName = container.getName();
            image = container.getImage();

            if (container.getEnv() != null &&
                    !container.getEnv().isEmpty()) {

                environment = container.getEnv()
                        .stream()
                        .filter(env ->
                                env.getName() != null &&
                                        env.getValue() != null
                        )
                        .collect(Collectors.toMap(
                                env -> env.getName(),
                                env -> env.getValue()
                        ));
            }
        }

        Integer desiredReplicas = null;

        if (replicaSet.getSpec() != null) {
            desiredReplicas = replicaSet.getSpec().getReplicas();
        }

        Integer currentReplicas = null;
        Integer readyReplicas = null;
        Integer availableReplicas = null;

        if (replicaSet.getStatus() != null) {

            currentReplicas =
                    replicaSet.getStatus().getReplicas();

            readyReplicas =
                    replicaSet.getStatus().getReadyReplicas();

            availableReplicas =
                    replicaSet.getStatus().getAvailableReplicas();
        }

        OffsetDateTime creationTimestamp = null;

        if (replicaSet.getMetadata() != null &&
                replicaSet.getMetadata().getCreationTimestamp() != null) {

            creationTimestamp = OffsetDateTime.parse(
                    replicaSet.getMetadata().getCreationTimestamp()
            );
        }

        return ReplicaSetResponse.builder()

                .name(
                        replicaSet.getMetadata().getName()
                )

                .namespace(
                        replicaSet.getMetadata().getNamespace()
                )

                .desiredReplicas(
                        desiredReplicas
                )

                .currentReplicas(
                        currentReplicas
                )

                .readyReplicas(
                        readyReplicas
                )

                .availableReplicas(
                        availableReplicas
                )

                .containerName(
                        containerName
                )

                .image(
                        image
                )

                .labels(
                        replicaSet.getMetadata().getLabels()
                )

                .environment(
                        environment
                )

                .creationTimestamp(
                        creationTimestamp
                )

                .build();
    }

    public ReplicaSetSummaryResponse toSummaryResponse(
            ReplicaSet replicaSet
    ) {

        Integer desiredReplicas = null;
        Integer currentReplicas = null;
        Integer readyReplicas = null;
        Integer availableReplicas = null;

        if (replicaSet.getSpec() != null) {

            desiredReplicas =
                    replicaSet.getSpec().getReplicas();
        }

        if (replicaSet.getStatus() != null) {

            currentReplicas =
                    replicaSet.getStatus().getReplicas();

            readyReplicas =
                    replicaSet.getStatus().getReadyReplicas();

            availableReplicas =
                    replicaSet.getStatus().getAvailableReplicas();
        }

        return ReplicaSetSummaryResponse.builder()

                .name(
                        replicaSet.getMetadata().getName()
                )

                .desiredReplicas(
                        desiredReplicas
                )

                .currentReplicas(
                        currentReplicas
                )

                .readyReplicas(
                        readyReplicas
                )

                .availableReplicas(
                        availableReplicas
                )

                .status(
                        determineStatus(
                                desiredReplicas,
                                currentReplicas,
                                readyReplicas
                        )
                )

                .build();
    }

    private String determineStatus(
            Integer desiredReplicas,
            Integer currentReplicas,
            Integer readyReplicas
    ) {

        if (desiredReplicas == null) {
            return "UNKNOWN";
        }

        if (desiredReplicas == 0) {
            return "SCALED_DOWN";
        }

        if (currentReplicas == null || currentReplicas == 0) {
            return "PENDING";
        }

        if (readyReplicas != null &&
                readyReplicas.equals(desiredReplicas)) {

            return "READY";
        }

        if (currentReplicas.equals(desiredReplicas)) {
            return "RUNNING";
        }

        return "SCALING";
    }
}