package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.PodResponse;
import com.kubemanager.cluster_service.dto.response.PodSummaryResponse;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class PodMapper {

    public PodSummaryResponse toSummaryResponse(
            Pod pod
    ) {

        return PodSummaryResponse.builder()
                .name(
                        pod.getMetadata().getName()
                )
                .namespace(
                        pod.getMetadata().getNamespace()
                )
                .status(
                        pod.getStatus() != null
                                ? pod.getStatus().getPhase()
                                : "Unknown"
                )
                .node(
                        pod.getSpec() != null
                                ? pod.getSpec().getNodeName()
                                : null
                )
                .podIp(
                        pod.getStatus() != null
                                ? pod.getStatus().getPodIP()
                                : null
                )
                .restartCount(
                        getRestartCount(pod)
                )
                .build();
    }

    public PodResponse toResponse(
            Pod pod
    ) {

        return PodResponse.builder()
                .name(
                        pod.getMetadata().getName()
                )
                .namespace(
                        pod.getMetadata().getNamespace()
                )
                .status(
                        pod.getStatus() != null
                                ? pod.getStatus().getPhase()
                                : "Unknown"
                )
                .node(
                        pod.getSpec() != null
                                ? pod.getSpec().getNodeName()
                                : null
                )
                .podIp(
                        pod.getStatus() != null
                                ? pod.getStatus().getPodIP()
                                : null
                )
                .hostIp(
                        pod.getStatus() != null
                                ? pod.getStatus().getHostIP()
                                : null
                )
                .qosClass(
                        pod.getStatus() != null
                                ? pod.getStatus().getQosClass()
                                : null
                )
                .serviceAccount(
                        pod.getSpec() != null
                                ? pod.getSpec().getServiceAccountName()
                                : null
                )
                .startTime(
                        pod.getStatus() != null &&
                                pod.getStatus().getStartTime() != null
                                ? OffsetDateTime.parse(
                                pod.getStatus().getStartTime()
                        )
                                : null
                )
                .labels(
                        pod.getMetadata().getLabels()
                )
                .annotations(
                        pod.getMetadata().getAnnotations()
                )
                .build();
    }

    private Integer getRestartCount(
            Pod pod
    ) {

        if (pod.getStatus() == null ||
                pod.getStatus().getContainerStatuses() == null) {

            return 0;
        }

        return pod.getStatus()
                .getContainerStatuses()
                .stream()
                .mapToInt(ContainerStatus::getRestartCount)
                .sum();
    }
}