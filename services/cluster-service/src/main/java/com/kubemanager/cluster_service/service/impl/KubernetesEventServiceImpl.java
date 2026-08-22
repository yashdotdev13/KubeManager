package com.kubemanager.cluster_service.service.impl;

import com.kubemanager.cluster_service.dto.response.KubernetesEventResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.KubernetesEventService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.Event;
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
public class KubernetesEventServiceImpl
        implements KubernetesEventService {

    private final ClusterRepository clusterRepository;

    private final KubernetesClientFactory kubernetesClientFactory;

    @Override
    public List<KubernetesEventResponse> getEvents(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Fetching Kubernetes events for clusterId={}, namespace={}",
                clusterId,
                namespace
        );

        Cluster cluster =
                clusterRepository.findById(clusterId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        ErrorCode.CLUSTER_NOT_FOUND,
                                        "Cluster not found."
                                )
                        );

        validateKubeConfig(cluster);

        validateNamespace(namespace);

        try (
                KubernetesClient client =
                        kubernetesClientFactory.createClient(
                                cluster.getEncryptedKubeConfig()
                        )
        ) {

            List<Event> events =
                    client.v1()
                            .events()
                            .inNamespace(namespace)
                            .list()
                            .getItems();

            if (events == null
                    || events.isEmpty()) {

                return List.of();
            }

            return events.stream()
                    .map(this::mapEvent)
                    .toList();

        } catch (
                ResourceNotFoundException
                | BadRequestException exception
        ) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch Kubernetes events " +
                            "for clusterId={}, namespace={}",
                    clusterId,
                    namespace,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.KUBERNETES_EVENTS_UNAVAILABLE,
                    "Unable to fetch Kubernetes events."
            );
        }
    }

    @Override
    public List<KubernetesEventResponse> getPodEvents(
            UUID clusterId,
            String namespace,
            String podName
    ) {

        log.info(
                "Fetching events for pod '{}' in namespace '{}' " +
                        "for cluster '{}'.",
                podName,
                namespace,
                clusterId
        );

        Cluster cluster =
                clusterRepository.findById(clusterId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        ErrorCode.CLUSTER_NOT_FOUND,
                                        "Cluster not found."
                                )
                        );

        validateKubeConfig(cluster);

        validateNamespace(namespace);

        if (podName == null
                || podName.isBlank()) {

            throw new BadRequestException(
                    ErrorCode.KUBERNETES_EVENTS_UNAVAILABLE,
                    "Pod name is required."
            );
        }

        try (
                KubernetesClient client =
                        kubernetesClientFactory.createClient(
                                cluster.getEncryptedKubeConfig()
                        )
        ) {

            List<Event> events =
                    client.v1()
                            .events()
                            .inNamespace(namespace)
                            .list()
                            .getItems();

            if (events == null
                    || events.isEmpty()) {

                return List.of();
            }

            return events.stream()
                    .filter(event ->
                            event.getInvolvedObject() != null
                                    && podName.equals(
                                    event.getInvolvedObject()
                                            .getName()
                            )
                    )
                    .map(this::mapEvent)
                    .toList();

        } catch (
                ResourceNotFoundException
                | BadRequestException exception
        ) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch events for pod '{}'",
                    podName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.KUBERNETES_EVENTS_UNAVAILABLE,
                    "Unable to fetch pod events."
            );
        }
    }

    private KubernetesEventResponse mapEvent(
            Event event
    ) {

        String source = null;

        if (event.getSource() != null) {

            source =
                    event.getSource()
                            .getComponent();
        }

        String involvedKind = null;

        String involvedName = null;

        String involvedNamespace = null;

        if (event.getInvolvedObject() != null) {

            involvedKind =
                    event.getInvolvedObject()
                            .getKind();

            involvedName =
                    event.getInvolvedObject()
                            .getName();

            involvedNamespace =
                    event.getInvolvedObject()
                            .getNamespace();
        }

        return KubernetesEventResponse.builder()
                .name(
                        event.getMetadata() != null
                                ? event.getMetadata().getName()
                                : null
                )
                .namespace(
                        involvedNamespace != null
                                ? involvedNamespace
                                : event.getMetadata() != null
                                ? event.getMetadata()
                                .getNamespace()
                                : null
                )
                .type(
                        event.getType()
                )
                .reason(
                        event.getReason()
                )
                .message(
                        event.getMessage()
                )
                .involvedKind(
                        involvedKind
                )
                .involvedName(
                        involvedName
                )
                .source(
                        source
                )
                .count(
                        event.getCount()
                )
                .firstTimestamp(
                        parseTimestamp(
                                event.getFirstTimestamp()
                        )
                )
                .lastTimestamp(
                        parseTimestamp(
                                event.getLastTimestamp()
                        )
                )
                .build();
    }

    private OffsetDateTime parseTimestamp(
            String timestamp
    ) {

        if (timestamp == null
                || timestamp.isBlank()) {

            return null;
        }

        try {

            return OffsetDateTime.parse(
                    timestamp
            );

        } catch (Exception exception) {

            log.debug(
                    "Unable to parse Kubernetes event timestamp: {}",
                    timestamp
            );

            return null;
        }
    }

    private void validateKubeConfig(
            Cluster cluster
    ) {

        if (cluster.getEncryptedKubeConfig() == null
                || cluster.getEncryptedKubeConfig().isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster kubeconfig is not available."
            );
        }
    }

    private void validateNamespace(
            String namespace
    ) {

        if (namespace == null
                || namespace.isBlank()) {

            throw new BadRequestException(
                    ErrorCode.KUBERNETES_EVENTS_UNAVAILABLE,
                    "Namespace is required."
            );
        }
    }
}
