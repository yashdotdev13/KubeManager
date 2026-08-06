package com.kubemanager.cluster_service.kubernates.service;

import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.web.multipart.MultipartFile;

public interface KubernetesConnectionService {

    /**
     * Used when user uploads a kubeconfig file.
     */
    ClusterMetadata connect(MultipartFile kubeConfig);

    /**
     * Used when kubeconfig is already stored in database.
     */
    ClusterMetadata connect(String kubeConfigContent);

    /**
     * Extract metadata from an already connected cluster.
     */
    ClusterMetadata fetchClusterMetadata(KubernetesClient client);

}