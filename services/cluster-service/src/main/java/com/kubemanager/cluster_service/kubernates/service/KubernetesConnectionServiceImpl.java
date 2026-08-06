package com.kubemanager.cluster_service.kubernates.service;


import com.kubemanager.cluster_service.enums.ClusterStatus;
import com.kubemanager.cluster_service.enums.PlatformType;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

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
}