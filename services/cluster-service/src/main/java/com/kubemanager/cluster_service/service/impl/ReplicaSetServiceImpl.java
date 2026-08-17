package com.kubemanager.cluster_service.service.impl;


import com.kubemanager.cluster_service.dto.request.CreateReplicaSetRequest;
import com.kubemanager.cluster_service.dto.request.ReplicaSetResponse;
import com.kubemanager.cluster_service.dto.request.ReplicaSetSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.ReplicaSetMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.ReplicaSetService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.ReplicaSetBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplicaSetServiceImpl implements ReplicaSetService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final ReplicaSetMapper replicaSetMapper;


    @Override
    public ReplicaSetResponse createReplicaSet(
            UUID clusterId,
            String namespace,
            CreateReplicaSetRequest request
    ) {

        log.info(
                "Creating ReplicaSet '{}' in namespace '{}' for cluster '{}'.",
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

            ReplicaSet existingReplicaSet =
                    client.apps()
                            .replicaSets()
                            .inNamespace(namespace)
                            .withName(request.getName())
                            .get();

            if (existingReplicaSet != null) {

                throw new BadRequestException(
                        ErrorCode.REPLICA_SET_ALREADY_EXISTS,
                        "ReplicaSet already exists."
                );
            }

            ReplicaSet replicaSet =
                    buildReplicaSet(namespace, request);

            ReplicaSet createdReplicaSet =
                    client.apps()
                            .replicaSets()
                            .inNamespace(namespace)
                            .resource(replicaSet)
                            .create();

            log.info(
                    "ReplicaSet '{}' created successfully.",
                    request.getName()
            );

            return replicaSetMapper.toResponse(createdReplicaSet);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create ReplicaSet '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.REPLICA_SET_CREATION_FAILED,
                    "Unable to create ReplicaSet."
            );
        }
    }

    @Override
    public List<ReplicaSetSummaryResponse> getReplicaSets(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Fetching ReplicaSets in namespace '{}' for cluster '{}'.",
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

            List<ReplicaSet> replicaSets =
                    client.apps()
                            .replicaSets()
                            .inNamespace(namespace)
                            .list()
                            .getItems();

            log.info(
                    "Found {} ReplicaSet(s) in namespace '{}'.",
                    replicaSets.size(),
                    namespace
            );

            return replicaSets.stream()
                    .map(replicaSetMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch ReplicaSets in namespace '{}'.",
                    namespace,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.REPLICA_SET_NOT_FOUND,
                    "Unable to fetch ReplicaSets."
            );
        }
    }

    @Override
    public ReplicaSetResponse getReplicaSet(
            UUID clusterId,
            String namespace,
            String replicaSetName
    ) {

        log.info(
                "Fetching ReplicaSet '{}' in namespace '{}' for cluster '{}'.",
                replicaSetName,
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

            ReplicaSet replicaSet =
                    client.apps()
                            .replicaSets()
                            .inNamespace(namespace)
                            .withName(replicaSetName)
                            .get();

            if (replicaSet == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.REPLICA_SET_NOT_FOUND,
                        "ReplicaSet not found."
                );
            }

            log.info(
                    "ReplicaSet '{}' fetched successfully.",
                    replicaSetName
            );

            return replicaSetMapper.toResponse(replicaSet);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch ReplicaSet '{}'.",
                    replicaSetName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.REPLICA_SET_NOT_FOUND,
                    "Unable to fetch ReplicaSet."
            );
        }
    }

    @Override
    public void deleteReplicaSet(
            UUID clusterId,
            String namespace,
            String replicaSetName
    ) {

        log.info(
                "Deleting ReplicaSet '{}' in namespace '{}' for cluster '{}'.",
                replicaSetName,
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

            ReplicaSet replicaSet =
                    client.apps()
                            .replicaSets()
                            .inNamespace(namespace)
                            .withName(replicaSetName)
                            .get();

            if (replicaSet == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.REPLICA_SET_NOT_FOUND,
                        "ReplicaSet not found."
                );
            }

            client.apps()
                    .replicaSets()
                    .inNamespace(namespace)
                    .withName(replicaSetName)
                    .delete();

            log.info(
                    "ReplicaSet '{}' deleted successfully.",
                    replicaSetName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete ReplicaSet '{}'.",
                    replicaSetName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.REPLICA_SET_DELETION_FAILED,
                    "Unable to delete ReplicaSet."
            );
        }
    }


    private ReplicaSet buildReplicaSet(
            String namespace,
            CreateReplicaSetRequest request
    ) {

        ContainerBuilder containerBuilder =
                new ContainerBuilder()
                        .withName(request.getContainerName())
                        .withImage(request.getImage());

        if (request.getCommand() != null &&
                !request.getCommand().isBlank()) {

            containerBuilder.withCommand(
                    "sh",
                    "-c",
                    request.getCommand()
            );
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

        Container container = containerBuilder.build();

        return new ReplicaSetBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(namespace)
                .withLabels(request.getLabels())
                .endMetadata()

                .withNewSpec()

                .withReplicas(request.getReplicas())

                .withSelector(
                        new LabelSelectorBuilder()
                                .withMatchLabels(request.getLabels())
                                .build()
                )

                .withNewTemplate()

                .withNewMetadata()
                .withLabels(request.getLabels())
                .endMetadata()

                .withNewSpec()
                .withRestartPolicy("Always")
                .withContainers(container)
                .endSpec()

                .endTemplate()

                .endSpec()

                .build();
    }
}
