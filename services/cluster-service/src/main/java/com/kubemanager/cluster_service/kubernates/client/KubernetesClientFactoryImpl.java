package com.kubemanager.cluster_service.kubernates.client;


import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class KubernetesClientFactoryImpl implements KubernetesClientFactory {



    @Override
    public KubernetesClient createClient(MultipartFile kubeConfig) {

        if (kubeConfig == null || kubeConfig.isEmpty()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Kubeconfig file is required."
            );
        }

        try {

            String kubeConfigContent = new String(
                    kubeConfig.getBytes(),
                    StandardCharsets.UTF_8
            );

            return createClient(kubeConfigContent);

        } catch (IOException exception) {

            log.error("Failed to read kubeconfig.", exception);

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Invalid kubeconfig file."
            );
        }
    }

    @Override
    public KubernetesClient createClient(String kubeConfigContent) {

        if (kubeConfigContent == null || kubeConfigContent.isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Kubeconfig content is required."
            );
        }

        try {

            Config config = Config.fromKubeconfig(kubeConfigContent);

            return new KubernetesClientBuilder()
                    .withConfig(config)
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to create Kubernetes client.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Invalid kubeconfig."
            );
        }
    }
}
