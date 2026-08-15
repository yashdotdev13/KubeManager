package com.kubemanager.cluster_service.service.Impl;

import com.kubemanager.cluster_service.dto.response.PodDescribeResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PodDescribeService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PodDescribeServiceImpl implements PodDescribeService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;

    @Override
    public PodDescribeResponse describePod(
            UUID clusterId,
            String namespace,
            String podName
    ) {

        log.info(
                "Describing pod '{}' in namespace '{}' for cluster '{}'.",
                podName,
                namespace,
                clusterId
        );
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));

        if (cluster.getEncryptedKubeConfig() == null ||
                cluster.getEncryptedKubeConfig().isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster kubeconfig is not available."
            );
        }

        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {

            Pod pod = client.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .get();

            if (pod == null) {
                throw new ResourceNotFoundException(
                        ErrorCode.POD_NOT_FOUND,
                        "Pod not found."
                );
            }
            if (pod.getMetadata() == null) {
                throw new BadRequestException(
                        ErrorCode.POD_DESCRIBE_FAILED,
                        "Pod metadata is not available."
                );
            }

            String name = pod.getMetadata().getName();
            String podNamespace = pod.getMetadata().getNamespace();
            String uid = pod.getMetadata().getUid();

            String startTime = null;

            String serviceAccount = null;
            String nodeName = null;
            String podIp = null;
            String hostIp = null;
            String phase = null;

            List<PodDescribeResponse.ContainerInfo> containers =
                    Collections.emptyList();

            List<PodDescribeResponse.ConditionInfo> conditions =
                    Collections.emptyList();

            if (pod.getStatus() != null) {

                nodeName = pod.getSpec() != null
                        ? pod.getSpec().getNodeName()
                        : null;

                podIp = pod.getStatus().getPodIP();
                hostIp = pod.getStatus().getHostIP();
                phase = pod.getStatus().getPhase();
                startTime = pod.getStatus().getStartTime();

                if (pod.getStatus().getContainerStatuses() != null) {

                    containers = pod.getStatus()
                            .getContainerStatuses()
                            .stream()
                            .map(this::mapContainer)
                            .collect(Collectors.toList());
                }

                if (pod.getStatus().getConditions() != null) {

                    conditions = pod.getStatus()
                            .getConditions()
                            .stream()
                            .map(this::mapCondition)
                            .collect(Collectors.toList());
                }
            }

            if (pod.getSpec() != null) {
                serviceAccount =
                        pod.getSpec().getServiceAccountName();
            }

            return PodDescribeResponse.builder()
                    .name(name)
                    .namespace(
                            podNamespace != null
                                    ? podNamespace
                                    : namespace
                    )
                    .uid(uid)
                    .nodeName(nodeName)
                    .podIp(podIp)
                    .hostIp(hostIp)
                    .phase(phase)
                    .startTime(startTime)
                    .serviceAccount(serviceAccount)
                    .containers(containers)
                    .conditions(conditions)
                    .build();

        } catch (ResourceNotFoundException exception) {
            throw exception;

        } catch (BadRequestException exception) {
            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to describe pod '{}'.",
                    podName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.POD_DESCRIBE_FAILED,
                    "Unable to describe pod."
            );
        }
    }

    private PodDescribeResponse.ContainerInfo mapContainer(
            ContainerStatus containerStatus
    ) {

        String state = null;
        String reason = null;

        ContainerState containerState =
                containerStatus.getState();

        if (containerState != null) {

            if (containerState.getRunning() != null) {
                state = "RUNNING";
                reason = containerState
                        .getRunning()
                        .getStartedAt();

            } else if (containerState.getWaiting() != null) {

                state = "WAITING";
                reason = containerState
                        .getWaiting()
                        .getReason();

            } else if (containerState.getTerminated() != null) {

                state = "TERMINATED";
                reason = containerState
                        .getTerminated()
                        .getReason();
            }
        }

        return PodDescribeResponse.ContainerInfo.builder()
                .name(containerStatus.getName())
                .image(containerStatus.getImage())
                .imageId(containerStatus.getImageID())
                .ready(String.valueOf(
                        containerStatus.getReady()
                ))
                .restartCount(String.valueOf(
                        containerStatus.getRestartCount()
                ))
                .state(state)
                .reason(reason)
                .build();
    }
    private PodDescribeResponse.ConditionInfo mapCondition(
            PodCondition condition
    ) {

        return PodDescribeResponse.ConditionInfo.builder()
                .type(condition.getType())
                .status(condition.getStatus())
                .reason(condition.getReason())
                .message(condition.getMessage())
                .lastTransitionTime(
                        condition.getLastTransitionTime()
                )
                .build();
    }
}