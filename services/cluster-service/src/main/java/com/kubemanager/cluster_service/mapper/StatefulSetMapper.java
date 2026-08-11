package com.kubemanager.cluster_service.mapper;


import com.kubemanager.cluster_service.dto.response.StatefulSetResponse;
import com.kubemanager.cluster_service.dto.response.StatefulSetSummaryResponse;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StatefulSetMapper {

    public StatefulSetResponse toResponse(
            StatefulSet statefulSet
    ) {

        String containerName = null;
        String image = null;
        Integer containerPort = null;
        Map<String, String> environment = null;

        if (statefulSet.getSpec() != null &&
                statefulSet.getSpec().getTemplate() != null &&
                statefulSet.getSpec()
                        .getTemplate()
                        .getSpec() != null &&
                statefulSet.getSpec()
                        .getTemplate()
                        .getSpec()
                        .getContainers() != null &&
                !statefulSet.getSpec()
                        .getTemplate()
                        .getSpec()
                        .getContainers()
                        .isEmpty()) {

            Container container =
                    statefulSet.getSpec()
                            .getTemplate()
                            .getSpec()
                            .getContainers()
                            .get(0);

            containerName = container.getName();
            image = container.getImage();

            if (container.getPorts() != null &&
                    !container.getPorts().isEmpty()) {

                containerPort =
                        container.getPorts()
                                .get(0)
                                .getContainerPort();
            }

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

        OffsetDateTime creationTimestamp = null;

        if (statefulSet.getMetadata() != null &&
                statefulSet.getMetadata()
                        .getCreationTimestamp() != null) {

            creationTimestamp = OffsetDateTime.parse(
                    statefulSet.getMetadata()
                            .getCreationTimestamp()
            );
        }

        Integer desiredReplicas = null;
        Integer currentReplicas = null;
        Integer readyReplicas = null;

        if (statefulSet.getSpec() != null) {

            desiredReplicas =
                    statefulSet.getSpec().getReplicas();
        }

        if (statefulSet.getStatus() != null) {

            currentReplicas =
                    statefulSet.getStatus().getCurrentReplicas();

            readyReplicas =
                    statefulSet.getStatus().getReadyReplicas();
        }

        return StatefulSetResponse.builder()

                .name(
                        statefulSet.getMetadata().getName()
                )

                .namespace(
                        statefulSet.getMetadata().getNamespace()
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

                .serviceName(
                        statefulSet.getSpec() != null
                                ? statefulSet.getSpec()
                                .getServiceName()
                                : null
                )

                .containerName(
                        containerName
                )

                .image(
                        image
                )

                .containerPort(
                        containerPort
                )

                .labels(
                        statefulSet.getMetadata() != null
                                ? statefulSet.getMetadata().getLabels()
                                : null
                )

                .environment(
                        environment
                )

                .creationTimestamp(
                        creationTimestamp
                )

                .build();
    }

    public StatefulSetSummaryResponse toSummaryResponse(
            StatefulSet statefulSet
    ) {

        Integer desiredReplicas = null;
        Integer currentReplicas = null;
        Integer readyReplicas = null;

        if (statefulSet.getSpec() != null) {

            desiredReplicas =
                    statefulSet.getSpec().getReplicas();
        }

        if (statefulSet.getStatus() != null) {

            currentReplicas =
                    statefulSet.getStatus().getCurrentReplicas();

            readyReplicas =
                    statefulSet.getStatus().getReadyReplicas();
        }

        return StatefulSetSummaryResponse.builder()

                .name(
                        statefulSet.getMetadata().getName()
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

                .serviceName(
                        statefulSet.getSpec() != null
                                ? statefulSet.getSpec()
                                .getServiceName()
                                : null
                )

                .build();
    }
}
