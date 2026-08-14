package com.kubemanager.cluster_service.service.Impl;


import com.kubemanager.cluster_service.dto.response.PodEventResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PodEventService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PodEventServiceImpl implements PodEventService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;

    @Override
    public List<PodEventResponse> getPodEvents(
            UUID clusterId,
            String namespace,
            String podName
    ) {

        log.info(
                "Fetching events for pod '{}' in namespace '{}' for cluster '{}'.",
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

            /*
             * First verify that the Pod exists.
             */
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

            /*
             * Fetch Events belonging to this Pod.
             *
             * Events are filtered using:
             * involvedObject.kind = Pod
             * involvedObject.name = podName
             */
            List<Event> events = client.v1()
                    .events()
                    .inNamespace(namespace)
                    .withField(
                            "involvedObject.kind",
                            "Pod"
                    )
                    .withField(
                            "involvedObject.name",
                            podName
                    )
                    .list()
                    .getItems();

            return events.stream()
                    .map(this::toResponse)
                    .toList();

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch events for pod '{}'.",
                    podName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.POD_EVENTS_UNAVAILABLE,
                    "Unable to fetch pod events."
            );
        }
    }

    private PodEventResponse toResponse(Event event) {

        String source = null;

        if (event.getSource() != null) {

            source = event.getSource().getComponent();

            if (source == null) {
                source = event.getSource().getHost();
            }
        }

        Integer count = event.getCount();

        OffsetDateTime firstTimestamp =
                parseTimestamp(event.getFirstTimestamp());

        OffsetDateTime lastTimestamp =
                parseTimestamp(event.getLastTimestamp());

        return PodEventResponse.builder()
                .type(event.getType())
                .reason(event.getReason())
                .message(event.getMessage())
                .source(source)
                .count(count)
                .firstTimestamp(firstTimestamp)
                .lastTimestamp(lastTimestamp)
                .build();
    }

    private OffsetDateTime parseTimestamp(String timestamp) {

        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }

        try {
            return OffsetDateTime.parse(timestamp);
        } catch (Exception exception) {
            return null;
        }
    }
}