package com.kubemanager.cluster_service.service.Impl;

import com.kubemanager.cluster_service.dto.request.CreateNamespaceRequest;
import com.kubemanager.cluster_service.dto.response.NamespaceResponse;
import com.kubemanager.cluster_service.dto.response.NamespaceSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.NamespaceMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.NamespaceService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class NamespaceServiceImpl implements NamespaceService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final NamespaceMapper namespaceMapper;


    @Override
    public List<NamespaceSummaryResponse> getNamespaces(UUID clusterId) {

        log.info("Fetching namespaces for cluster: {}", clusterId);

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

            NamespaceList namespaceList =
                    client.namespaces().list();

            log.info(
                    "Found {} namespaces.",
                    namespaceList.getItems().size()
            );

            return namespaceList.getItems()
                    .stream()
                    .map(namespaceMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch namespaces.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch namespaces from Kubernetes cluster."
            );
        }
    }

    @Override
    public NamespaceResponse getNamespace(UUID clusterId, String namespace) {


        log.info("Fetching namespace '{}' for cluster '{}'",namespace, clusterId);

        Cluster cluster =   clusterRepository.findById(clusterId)
                .orElseThrow(()->new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "cluster not found"
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

            Namespace kubernetesNamespace = client.namespaces()
                    .withName(namespace)
                    .get();

            if (kubernetesNamespace == null) {

                throw new ResourceNotFoundException(
                                ErrorCode.NOT_FOUND,
                        "Namespace not found."
                );
            }

            return namespaceMapper.toResponse(kubernetesNamespace);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch namespace '{}'",
                    namespace,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch namespace."
            );
        }
    }

    @Override
    public NamespaceResponse createNamespace(
            UUID clusterId,
            CreateNamespaceRequest request
    ) {

        if (request == null) {

            throw new BadRequestException(
                    ErrorCode.INVALID_REQUEST,
                    "Request body cannot be null."
            );
        }

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

            Namespace existingNamespace = client.namespaces()
                    .withName(request.getName())
                    .get();

            if (existingNamespace != null) {

                throw new BadRequestException(
                        ErrorCode.NAMESPACE_ALREADY_EXISTS,
                        "Namespace already exists."
                );
            }

            Namespace namespace = new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(request.getName())
                    .endMetadata()
                    .build();

            Namespace createdNamespace = client.namespaces()
                    .resource(namespace)
                    .create();

            log.info(
                    "Namespace '{}' created successfully.",
                    request.getName()
            );

            return namespaceMapper.toResponse(createdNamespace);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create namespace '{}'",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to create namespace."
            );
        }
    }

    @Override
    public void deleteNamespace(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Deleting namespace '{}' from cluster '{}'",
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

            Namespace existingNamespace = client.namespaces()
                    .withName(namespace)
                    .get();

            if (existingNamespace == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.NAMESPACE_NOT_FOUND,
                        "Namespace not found."
                );
            }

            boolean deleted = client.namespaces()
                    .withName(namespace)
                    .delete()
                    .size() > 0;

            if (!deleted) {

                throw new BadRequestException(
                        ErrorCode.NAMESPACE_DELETE_FAILED,
                        "Failed to delete namespace."
                );
            }

            log.info(
                    "Namespace '{}' deleted successfully.",
                    namespace
            );

        } catch (ResourceNotFoundException | BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete namespace '{}'",
                    namespace,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to delete namespace."
            );
        }
    }

}
