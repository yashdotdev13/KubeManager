package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.PodSummaryResponse;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import org.springframework.stereotype.Component;

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