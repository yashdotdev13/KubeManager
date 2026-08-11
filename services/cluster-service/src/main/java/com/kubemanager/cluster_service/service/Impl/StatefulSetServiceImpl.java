package com.kubemanager.cluster_service.service.Impl;


import com.kubemanager.cluster_service.dto.request.CreateStatefulSetRequest;
import com.kubemanager.cluster_service.dto.response.StatefulSetResponse;
import com.kubemanager.cluster_service.dto.response.StatefulSetSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.StatefulSetMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.StatefulSetService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSetBuilder;
import io.fabric8.kubernetes.api.model.apps.StatefulSetSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatefulSetServiceImpl implements StatefulSetService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final StatefulSetMapper statefulSetMapper;

    @Override
    public StatefulSetResponse createStatefulSet(
            UUID clusterId,
            String namespace,
            CreateStatefulSetRequest request
    ) {

        log.info(
                "Creating StatefulSet '{}' in namespace '{}' for cluster '{}'.",
                request.getName(),
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

            StatefulSet existingStatefulSet =
                    client.apps()
                            .statefulSets()
                            .inNamespace(namespace)
                            .withName(request.getName())
                            .get();

            if (existingStatefulSet != null) {

                throw new BadRequestException(
                        ErrorCode.STATEFUL_SET_ALREADY_EXISTS,
                        "StatefulSet already exists."
                );
            }

            StatefulSet statefulSet =
                    buildStatefulSet(namespace, request);

            StatefulSet createdStatefulSet =
                    client.apps()
                            .statefulSets()
                            .inNamespace(namespace)
                            .resource(statefulSet)
                            .create();

            log.info(
                    "StatefulSet '{}' created successfully.",
                    request.getName()
            );

            return statefulSetMapper.toResponse(
                    createdStatefulSet
            );

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create StatefulSet '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.STATEFUL_SET_CREATION_FAILED,
                    "Unable to create StatefulSet."
            );
        }
    }

    private StatefulSet buildStatefulSet(
            String namespace,
            CreateStatefulSetRequest request
    ) {

        ContainerBuilder containerBuilder =
                new ContainerBuilder()
                        .withName(request.getContainerName())
                        .withImage(request.getImage());

        if (request.getContainerPort() != null) {

            containerBuilder
                    .addNewPort()
                    .withContainerPort(request.getContainerPort())
                    .endPort();
        }

        if (request.getEnvironment() != null &&
                !request.getEnvironment().isEmpty()) {

            request.getEnvironment()
                    .forEach((key, value) ->
                            containerBuilder
                                    .addNewEnv()
                                    .withName(key)
                                    .withValue(value)
                                    .endEnv()
                    );
        }

        Container container =
                containerBuilder.build();

        return new StatefulSetBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(namespace)
                .withLabels(request.getLabels())
                .endMetadata()

                .withNewSpec()

                .withServiceName(
                        request.getServiceName()
                )

                .withReplicas(
                        request.getReplicas()
                )

                .withNewSelector()
                .withMatchLabels(
                        request.getLabels()
                )
                .endSelector()

                .withTemplate(
                        new PodTemplateSpecBuilder()

                                .withNewMetadata()
                                .withLabels(
                                        request.getLabels()
                                )
                                .endMetadata()

                                .withNewSpec()
                                .withContainers(container)
                                .endSpec()

                                .build()
                )

                .endSpec()

                .build();
    }

    @Override
    public List<StatefulSetSummaryResponse> getStatefulSets(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Fetching StatefulSets in namespace '{}' for cluster '{}'.",
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

            List<StatefulSet> statefulSets =
                    client.apps()
                            .statefulSets()
                            .inNamespace(namespace)
                            .list()
                            .getItems();

            log.info(
                    "Found {} StatefulSet(s) in namespace '{}'.",
                    statefulSets.size(),
                    namespace
            );

            return statefulSets.stream()
                    .map(statefulSetMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch StatefulSets in namespace '{}'.",
                    namespace,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.STATEFUL_SET_NOT_FOUND,
                    "Unable to fetch StatefulSets."
            );
        }
    }

    @Override
    public StatefulSetResponse getStatefulSet(
            UUID clusterId,
            String namespace,
            String statefulSetName
    ) {

        log.info(
                "Fetching StatefulSet '{}' in namespace '{}' for cluster '{}'.",
                statefulSetName,
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

            StatefulSet statefulSet =
                    client.apps()
                            .statefulSets()
                            .inNamespace(namespace)
                            .withName(statefulSetName)
                            .get();

            if (statefulSet == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.STATEFUL_SET_NOT_FOUND,
                        "StatefulSet not found."
                );
            }

            log.info(
                    "StatefulSet '{}' fetched successfully.",
                    statefulSetName
            );

            return statefulSetMapper.toResponse(
                    statefulSet
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch StatefulSet '{}'.",
                    statefulSetName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.STATEFUL_SET_NOT_FOUND,
                    "Unable to fetch StatefulSet."
            );
        }
    }

    @Override
    public void deleteStatefulSet(
            UUID clusterId,
            String namespace,
            String statefulSetName
    ) {

        log.info(
                "Deleting StatefulSet '{}' in namespace '{}' for cluster '{}'.",
                statefulSetName,
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

            StatefulSet statefulSet =
                    client.apps()
                            .statefulSets()
                            .inNamespace(namespace)
                            .withName(statefulSetName)
                            .get();

            if (statefulSet == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.STATEFUL_SET_NOT_FOUND,
                        "StatefulSet not found."
                );
            }

            client.apps()
                    .statefulSets()
                    .inNamespace(namespace)
                    .withName(statefulSetName)
                    .delete();

            log.info(
                    "StatefulSet '{}' deleted successfully.",
                    statefulSetName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete StatefulSet '{}'.",
                    statefulSetName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.STATEFUL_SET_DELETION_FAILED,
                    "Unable to delete StatefulSet."
            );
        }
    }
}
