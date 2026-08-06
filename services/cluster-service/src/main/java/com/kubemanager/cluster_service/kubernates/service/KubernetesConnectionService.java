package com.kubemanager.cluster_service.kubernates.service;

import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.web.multipart.MultipartFile;

public interface KubernetesConnectionService {

    ClusterMetadata connect(MultipartFile kubeConfig);



    ClusterMetadata fetchClusterMetadata(KubernetesClient client);
}
