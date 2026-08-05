package com.kubemanager.cluster_service.kubernates.client;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.web.multipart.MultipartFile;

public interface  KubernetesClientFactory {


    KubernetesClient createClient(MultipartFile kubeConfig);
}
