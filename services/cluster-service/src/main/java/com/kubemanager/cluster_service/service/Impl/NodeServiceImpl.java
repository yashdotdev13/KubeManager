package com.kubemanager.cluster_service.service.Impl;


import com.kubemanager.cluster_service.dto.response.NodeOperationResponse;
import com.kubemanager.cluster_service.dto.response.NodeResponse;
import com.kubemanager.cluster_service.dto.response.NodeSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.NodeMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.NodeService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {


    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;

    private final NodeMapper nodeMapper;


    @Override
    public List<NodeSummaryResponse> getNodes(UUID clusterId) {

        log.info("Fetching nodes for cluster: {}", clusterId);

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

            NodeList nodeList = client.nodes().list();

            log.info(
                    "Found {} nodes.",
                    nodeList.getItems().size()
            );

            return nodeList.getItems()
                    .stream()
                    .map(nodeMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch nodes.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch nodes."
            );
        }
    }

    @Override
    public NodeResponse getNode(
            UUID clusterId,
            String nodeName
    ) {

        log.info(
                "Fetching node '{}' for cluster '{}'",
                nodeName,
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

            io.fabric8.kubernetes.api.model.Node node =
                    client.nodes()
                            .withName(nodeName)
                            .get();

            if (node == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.NODE_NOT_FOUND,
                        "Node not found."
                );
            }

            log.info(
                    "Successfully fetched node '{}'",
                    nodeName
            );

            return nodeMapper.toResponse(node);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch node '{}'",
                    nodeName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch node."
            );
        }
    }

    @Override
    public NodeOperationResponse cordonNode(
            UUID clusterId,
            String nodeName
    ) {

        log.info(
                "Cordoning node '{}' for cluster '{}'.",
                nodeName,
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

            io.fabric8.kubernetes.api.model.Node node =
                    client.nodes()
                            .withName(nodeName)
                            .get();

            if (node == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.NODE_NOT_FOUND,
                        "Node not found."
                );
            }

            if (node.getSpec() != null &&
                    Boolean.TRUE.equals(
                            node.getSpec().getUnschedulable()
                    )) {

                log.info(
                        "Node '{}' is already cordoned.",
                        nodeName
                );

                return NodeOperationResponse.builder()
                        .nodeName(nodeName)
                        .status("ALREADY_CORDONED")
                        .message(
                                "Node is already cordoned."
                        )
                        .build();
            }

            node.getSpec().setUnschedulable(true);

            client.nodes()
                    .withName(nodeName)
                    .patch(node);

            log.info(
                    "Node '{}' cordoned successfully.",
                    nodeName
            );

            return NodeOperationResponse.builder()
                    .nodeName(nodeName)
                    .status("CORDONED")
                    .message(
                            "Node cordoned successfully. "
                                    + "New Pods will not be scheduled on this node."
                    )
                    .build();

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to cordon node '{}'.",
                    nodeName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.NODE_OPERATION_FAILED,
                    "Unable to cordon node."
            );
        }
    }

    @Override
    public NodeOperationResponse uncordonNode(
            UUID clusterId,
            String nodeName
    ) {

        log.info(
                "Uncordoning node '{}' for cluster '{}'.",
                nodeName,
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

            io.fabric8.kubernetes.api.model.Node node =
                    client.nodes()
                            .withName(nodeName)
                            .get();

            if (node == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.NODE_NOT_FOUND,
                        "Node not found."
                );
            }
            if (node.getSpec() == null ||
                    !Boolean.TRUE.equals(
                            node.getSpec().getUnschedulable()
                    )) {

                log.info(
                        "Node '{}' is already schedulable.",
                        nodeName
                );
                return NodeOperationResponse.builder()
                        .nodeName(nodeName)
                        .status("ALREADY_UNCORDONED")
                        .message(
                                "Node is already schedulable."
                        )
                        .build();
            }

            node.getSpec().setUnschedulable(false);

            client.nodes()
                    .withName(nodeName)
                    .patch(node);

            log.info(
                    "Node '{}' uncordoned successfully.",
                    nodeName
            );

            return NodeOperationResponse.builder()
                    .nodeName(nodeName)
                    .status("UNCORDONED")
                    .message(
                            "Node uncordoned successfully. "
                                    + "New Pods can now be scheduled on this node."
                    )
                    .build();

        } catch (ResourceNotFoundException exception) {
            throw exception;

        } catch (BadRequestException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Failed to uncordon node '{}'.",
                    nodeName,
                    exception
            );
            throw new BadRequestException(
                    ErrorCode.NODE_OPERATION_FAILED,
                    "Unable to uncordon node."
            );
        }
    }
}
