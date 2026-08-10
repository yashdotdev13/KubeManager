package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateSecretRequest;
import com.kubemanager.cluster_service.dto.response.SecretResponse;
import com.kubemanager.cluster_service.dto.response.SecretSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface SecretService {

    SecretResponse createSecret(
            UUID clusterId,
            CreateSecretRequest request
    );

    List<SecretSummaryResponse> getSecrets(
            UUID clusterId,
            String namespace
    );

    SecretResponse getSecret(
            UUID clusterId,
            String namespace,
            String secretName
    );

    void deleteSecret(
            UUID clusterId,
            String namespace,
            String secretName
    );
}