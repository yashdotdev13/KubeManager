package com.kubemanager.cluster_service.kubernates.service;


import com.kubemanager.cluster_service.dto.response.NodeResponse;
import com.kubemanager.cluster_service.enums.ClusterStatus;
import com.kubemanager.cluster_service.enums.PlatformType;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeStatus;
import io.fabric8.kubernetes.api.model.Quantity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KubernetesConnectionServiceImpl
        implements KubernetesConnectionService {

    private final KubernetesClientFactory kubernetesClientFactory;

    @Override
    public ClusterMetadata connect(
            MultipartFile kubeConfig
    ) {

        try (
                KubernetesClient client =
                        kubernetesClientFactory.createClient(kubeConfig)
        ) {

            log.info("Attempting to connect to Kubernetes cluster.");

            VersionInfo version =
                    client.getKubernetesVersion();

            NodeList nodeList =
                    client.nodes().list();

            NamespaceList namespaceList =
                    client.namespaces().list();

            ClusterMetadata metadata =
                    ClusterMetadata.builder()
                            .apiServer(
                                    client.getConfiguration().getMasterUrl()
                            )
                            .kubernetesVersion(
                                    version.getGitVersion()
                            )
                            .platform(
                                    detectPlatform(client)
                            )
                            .nodeCount(
                                    nodeList.getItems().size()
                            )
                            .namespaceCount(
                                    namespaceList.getItems().size()
                            )
                            .status(
                                    ClusterStatus.CONNECTED
                            )
                            .lastHealthCheck(
                                    LocalDateTime.now()
                            )
                            .build();

            log.info(
                    "Successfully connected to Kubernetes cluster. Version={}, Nodes={}, Namespaces={}",
                    metadata.getKubernetesVersion(),
                    metadata.getNodeCount(),
                    metadata.getNamespaceCount()
            );

            return metadata;

        } catch (Exception exception) {

            log.error(
                    "Failed to connect to Kubernetes cluster.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to connect to the Kubernetes cluster using the provided kubeconfig."
            );
        }
    }

    @Override
    public ClusterMetadata connect(String kubeConfigContent) {

        try (
                KubernetesClient client =
                        kubernetesClientFactory.createClient(kubeConfigContent)
        ) {

            log.info("Attempting to connect to Kubernetes cluster.");

            ClusterMetadata metadata = fetchClusterMetadata(client);

            log.info(
                    "Successfully connected to Kubernetes cluster. Version={}, Nodes={}, Namespaces={}",
                    metadata.getKubernetesVersion(),
                    metadata.getNodeCount(),
                    metadata.getNamespaceCount()
            );

            return metadata;

        } catch (Exception exception) {

            log.error(
                    "Failed to connect to Kubernetes cluster.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to connect to the Kubernetes cluster using the provided kubeconfig."
            );
        }
    }

    @Override
    public ClusterMetadata fetchClusterMetadata(KubernetesClient client) {

        VersionInfo versionInfo = client.getKubernetesVersion();

        int nodeCount = client.nodes()
                .list()
                .getItems()
                .size();

        int namespaceCount = client.namespaces()
                .list()
                .getItems()
                .size();

        String apiServer = client.getConfiguration().getMasterUrl();

        PlatformType platform = detectPlatform(client);

        return ClusterMetadata.builder()
                .apiServer(apiServer)
                .kubernetesVersion(versionInfo.getGitVersion())
                .platform(platform)
                .nodeCount(nodeCount)
                .namespaceCount(namespaceCount)
                .status(ClusterStatus.CONNECTED)
                .lastHealthCheck(LocalDateTime.now())
                .build();
    }

    @Override
    public List<NodeResponse> getNodes(
            KubernetesClient client
    ) {

        NodeList nodeList = client.nodes().list();

        return nodeList.getItems()
                .stream()
                .map(this::mapNode)
                .toList();
    }

    private PlatformType detectPlatform(
            KubernetesClient client
    ) {

        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return PlatformType.WINDOWS;
        }

        if (osName.contains("mac")) {
            return PlatformType.MACOS;
        }

        if (osName.contains("linux")) {

            String masterUrl =
                    client.getConfiguration().getMasterUrl();

            if (masterUrl != null &&
                    masterUrl.toLowerCase().contains("docker")) {

                return PlatformType.DOCKER_DESKTOP;
            }

            return PlatformType.LINUX;
        }

        return PlatformType.OTHER;
    }


    private String getNodeAddress(
            Node node,
            String addressType
    ) {

        if (node == null
                || node.getStatus() == null
                || node.getStatus().getAddresses() == null) {

            return null;
        }

        return node.getStatus()
                .getAddresses()
                .stream()
                .filter(address ->
                        addressType.equals(address.getType())
                )
                .map(address ->
                        address.getAddress()
                )
                .findFirst()
                .orElse(null);
    }

    private NodeResponse mapNode(Node node) {

        NodeStatus nodeStatus = node.getStatus();

        NodeSystemInfo nodeInfo =
                nodeStatus != null
                        ? nodeStatus.getNodeInfo()
                        : null;

        return NodeResponse.builder()

                .name(
                        node.getMetadata().getName()
                )

                .status(
                        getNodeStatus(node)
                )

                .role(
                        getNodeRole(node)
                )

                .kubernetesVersion(
                        nodeInfo != null
                                ? nodeInfo.getKubeletVersion()
                                : null
                )

                .operatingSystem(
                        nodeInfo != null
                                ? nodeInfo.getOperatingSystem()
                                : null
                )

                .architecture(
                        nodeInfo != null
                                ? nodeInfo.getArchitecture()
                                : null
                )

                .kernelVersion(
                        nodeInfo != null
                                ? nodeInfo.getKernelVersion()
                                : null
                )

                .containerRuntime(
                        nodeInfo != null
                                ? nodeInfo.getContainerRuntimeVersion()
                                : null
                )

                .osImage(
                        nodeInfo != null
                                ? nodeInfo.getOsImage()
                                : null
                )

                .internalIp(
                        getNodeAddress(node, "InternalIP")
                )

                .externalIp(
                        getNodeAddress(node, "ExternalIP")
                )
                .labels(
                        node.getMetadata().getLabels()
                )

                .capacity(
                        convertResources(
                                nodeStatus != null
                                        ? nodeStatus.getCapacity()
                                        : null
                        )
                )

                .allocatable(
                        convertResources(
                                nodeStatus != null
                                        ? nodeStatus.getAllocatable()
                                        : null
                        )
                )

                .build();
    }


    private Map<String, String> convertResources(
            Map<String, Quantity> resources
    ) {

        if (resources == null) {
            return null;
        }

        return resources.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getAmount()
                ));
    }

    private String getNodeStatus(Node node) {

        if (node == null
                || node.getStatus() == null
                || node.getStatus().getConditions() == null) {

            return "UNKNOWN";
        }

        return node.getStatus()
                .getConditions()
                .stream()
                .filter(condition ->
                        "Ready".equals(condition.getType())
                )
                .map(condition ->
                        "True".equals(condition.getStatus())
                                ? "READY"
                                : "NOT_READY"
                )
                .findFirst()
                .orElse("UNKNOWN");
    }


    private String getNodeRole(Node node) {

        if (node == null
                || node.getMetadata() == null
                || node.getMetadata().getLabels() == null) {

            return "WORKER";
        }

        Map<String, String> labels =
                node.getMetadata().getLabels();

        if (labels.containsKey("node-role.kubernetes.io/control-plane")) {
            return "CONTROL_PLANE";
        }

        if (labels.containsKey("node-role.kubernetes.io/master")) {
            return "CONTROL_PLANE";
        }

        return "WORKER";
    }
}