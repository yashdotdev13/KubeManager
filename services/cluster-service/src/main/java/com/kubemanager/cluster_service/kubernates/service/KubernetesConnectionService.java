package com.kubemanager.cluster_service.kubernates.service;

import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import org.springframework.web.multipart.MultipartFile;

public interface KubernetesConnectionService {

    ClusterMetadata connect(MultipartFile kubeConfig);
}
