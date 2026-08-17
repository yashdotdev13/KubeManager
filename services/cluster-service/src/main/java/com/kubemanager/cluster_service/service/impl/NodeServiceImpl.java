package com.kubemanager.cluster_service.service.impl;


import com.kubemanager.cluster_service.dto.request.NodeDrainRequest;
import com.kubemanager.cluster_service.dto.response.NodeDrainResponse;
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
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.policy.v1.Eviction;
import io.fabric8.kubernetes.api.model.policy.v1.EvictionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Override
    public NodeDrainResponse drainNode(
            UUID clusterId,
            String nodeName,
            NodeDrainRequest request
    ) {

        log.info(
                "Draining node '{}' for cluster '{}'.",
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

        if (request == null) {
            request = new NodeDrainRequest(
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        int timeoutSeconds =
                request.getTimeoutSecondsOrDefault();

        int gracePeriodSeconds =
                request.getGracePeriodSecondsOrDefault();

        boolean ignoreDaemonSets =
                request.isIgnoreDaemonSets();

        boolean deleteEmptyDirData =
                request.isDeleteEmptyDirData();

        boolean force =
                request.isForce();

        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {

            /*
             * 1. Verify node exists
             */
            Node node = client.nodes()
                    .withName(nodeName)
                    .get();

            if (node == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.NODE_NOT_FOUND,
                        "Node not found."
                );
            }

            /*
             * 2. Cordon node first.
             *
             * A drained node must not receive new workloads
             * while existing workloads are being evicted.
             */
            if (node.getSpec() == null ||
                    !Boolean.TRUE.equals(
                            node.getSpec().getUnschedulable()
                    )) {

                client.nodes()
                        .withName(nodeName)
                        .edit(currentNode ->
                                new NodeBuilder(currentNode)
                                        .editSpec()
                                        .withUnschedulable(true)
                                        .endSpec()
                                        .build()
                        );

                log.info(
                        "Node '{}' cordoned successfully.",
                        nodeName
                );
            } else {

                log.info(
                        "Node '{}' is already cordoned.",
                        nodeName
                );
            }

            /*
             * 3. Get all pods running on this node.
             *
             * spec.nodeName is used as the Kubernetes field selector.
             */
            PodList podList = client.pods()
                    .inAnyNamespace()
                    .withField(
                            "spec.nodeName",
                            nodeName
                    )
                    .list();

            List<String> evictedPods =
                    new ArrayList<>();

            List<String> skippedPods =
                    new ArrayList<>();

            List<String> failedPods =
                    new ArrayList<>();

            log.info(
                    "Found {} pods on node '{}'.",
                    podList.getItems().size(),
                    nodeName
            );

            /*
             * 4. Process every pod.
             */
            for (Pod pod : podList.getItems()) {

                String podName =
                        pod.getMetadata().getName();

                String namespace =
                        pod.getMetadata().getNamespace();

                if (podName == null || namespace == null) {
                    continue;
                }

                /*
                 * Skip pods that are already terminating.
                 */
                if (pod.getMetadata().getDeletionTimestamp() != null) {

                    skippedPods.add(podName);

                    log.debug(
                            "Skipping pod '{}' because it is already terminating.",
                            podName
                    );

                    continue;
                }

                /*
                 * 4.1 Skip DaemonSet pods.
                 *
                 * DaemonSet pods are normally recreated on the same
                 * node and should not be evicted during a normal drain.
                 */
                if (ignoreDaemonSets &&
                        isDaemonSetPod(pod)) {

                    skippedPods.add(podName);

                    log.info(
                            "Skipping DaemonSet pod '{}'.",
                            podName
                    );

                    continue;
                }

                /*
                 * 4.2 Skip mirror/static pods.
                 *
                 * Kubernetes mirror pods are managed by the kubelet
                 * and cannot be safely evicted through the normal API.
                 */
                if (isMirrorPod(pod)) {

                    skippedPods.add(podName);

                    log.info(
                            "Skipping mirror pod '{}'.",
                            podName
                    );

                    continue;
                }

                /*
                 * 4.3 Check whether pod has a controller.
                 */
                boolean hasController =
                        hasControllerOwner(pod);

                if (!hasController && !force) {

                    skippedPods.add(podName);

                    log.info(
                            "Skipping unmanaged pod '{}' because force=false.",
                            podName
                    );

                    continue;
                }

                /*
                 * 4.4 Check emptyDir volumes.
                 *
                 * If the pod uses emptyDir and the caller did not
                 * explicitly allow deletion of emptyDir data, skip it.
                 */
                if (hasEmptyDirVolume(pod) &&
                        !deleteEmptyDirData) {

                    skippedPods.add(podName);

                    log.info(
                            "Skipping pod '{}' because it uses emptyDir storage.",
                            podName
                    );

                    continue;
                }

                /*
                 * 5. Try normal Kubernetes eviction.
                 *
                 * Eviction respects PodDisruptionBudgets.
                 */
                try {

                    Eviction eviction =
                            new EvictionBuilder()
                                    .withNewMetadata()
                                    .withName(podName)
                                    .withNamespace(namespace)
                                    .endMetadata()
                                    .withDeleteOptions(
                                            new DeleteOptionsBuilder()
                                                    .withGracePeriodSeconds(
                                                            (long) gracePeriodSeconds
                                                    )
                                                    .build()
                                    )
                                    .build();

                    boolean evicted = client.pods()
                            .inNamespace(namespace)
                            .withName(podName)
                            .evict(eviction);

                    if (evicted) {

                        evictedPods.add(podName);

                        log.info(
                                "Pod '{}' evicted successfully.",
                                podName
                        );

                    } else {

                        /*
                         * Eviction request was not accepted.
                         *
                         * If force=true, fall back to force deletion.
                         */
                        if (force) {

                            forceDeletePod(
                                    client,
                                    namespace,
                                    podName
                            );

                            evictedPods.add(podName);

                            log.warn(
                                    "Pod '{}' force deleted after eviction failed.",
                                    podName
                            );

                        } else {

                            failedPods.add(podName);

                            log.warn(
                                    "Failed to evict pod '{}'.",
                                    podName
                            );
                        }
                    }

                } catch (Exception evictionException) {

                    log.warn(
                            "Eviction failed for pod '{}'.",
                            podName,
                            evictionException
                    );

                    /*
                     * Force fallback.
                     *
                     * This is intentionally only used when the caller
                     * explicitly requested force=true.
                     */
                    if (force) {

                        try {

                            forceDeletePod(
                                    client,
                                    namespace,
                                    podName
                            );

                            evictedPods.add(podName);

                            log.warn(
                                    "Pod '{}' force deleted.",
                                    podName
                            );

                        } catch (Exception forceException) {

                            failedPods.add(podName);

                            log.error(
                                    "Force deletion failed for pod '{}'.",
                                    podName,
                                    forceException
                            );
                        }

                    } else {

                        failedPods.add(podName);
                    }
                }
            }

            /*
             * 6. Build final response.
             */
            String status;

            if (failedPods.isEmpty()) {
                status = "DRAINED";
            } else if (!evictedPods.isEmpty()) {
                status = "PARTIALLY_DRAINED";
            } else {
                status = "DRAIN_FAILED";
            }

            String message =
                    "Node drain completed. " +
                            "Evicted pods: " + evictedPods.size() +
                            ", skipped pods: " + skippedPods.size() +
                            ", failed pods: " + failedPods.size() +
                            ".";

            log.info(
                    "Node '{}' drain completed. " +
                            "Evicted={}, skipped={}, failed={}.",
                    nodeName,
                    evictedPods.size(),
                    skippedPods.size(),
                    failedPods.size()
            );

            return NodeDrainResponse.builder()
                    .nodeName(nodeName)
                    .status(status)
                    .cordoned(true)
                    .evictedPods(evictedPods)
                    .skippedPods(skippedPods)
                    .failedPods(failedPods)
                    .message(message)
                    .build();

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to drain node '{}'.",
                    nodeName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.NODE_DRAIN_FAILED,
                    "Unable to drain node."
            );
        }
    }


    /*
     * ------------------------------------------------------------
     * Helper methods
     * ------------------------------------------------------------
     */


    private boolean isDaemonSetPod(Pod pod) {

        if (pod.getMetadata() == null ||
                pod.getMetadata().getOwnerReferences() == null) {

            return false;
        }

        return pod.getMetadata()
                .getOwnerReferences()
                .stream()
                .anyMatch(owner ->
                        "DaemonSet".equals(owner.getKind())
                );
    }


    private boolean hasControllerOwner(Pod pod) {

        if (pod.getMetadata() == null ||
                pod.getMetadata().getOwnerReferences() == null) {

            return false;
        }

        return pod.getMetadata()
                .getOwnerReferences()
                .stream()
                .anyMatch(owner ->
                        Boolean.TRUE.equals(
                                owner.getController()
                        )
                );
    }


    private boolean isMirrorPod(Pod pod) {

        if (pod.getMetadata() == null ||
                pod.getMetadata().getAnnotations() == null) {

            return false;
        }

        return pod.getMetadata()
                .getAnnotations()
                .containsKey(
                        "kubernetes.io/config.mirror"
                );
    }


    private boolean hasEmptyDirVolume(Pod pod) {

        if (pod.getSpec() == null ||
                pod.getSpec().getVolumes() == null) {

            return false;
        }

        return pod.getSpec()
                .getVolumes()
                .stream()
                .anyMatch(volume ->
                        volume.getEmptyDir() != null
                );
    }


    private void forceDeletePod(
            KubernetesClient client,
            String namespace,
            String podName
    ) {

        client.pods()
                .inNamespace(namespace)
                .withName(podName)
                .withGracePeriod(0)
                .delete();
    }
}
