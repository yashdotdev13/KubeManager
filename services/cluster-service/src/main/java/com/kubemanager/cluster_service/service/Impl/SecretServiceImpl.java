package com.kubemanager.cluster_service.service.Impl;


import com.kubemanager.cluster_service.dto.request.CreateSecretRequest;
import com.kubemanager.cluster_service.dto.response.SecretResponse;
import com.kubemanager.cluster_service.dto.response.SecretSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.SecretMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.SecretService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecretServiceImpl implements SecretService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final SecretMapper secretMapper;

    @Override
    public SecretResponse createSecret(
            UUID clusterId,
            CreateSecretRequest request
    ) {

        log.info(
                "Creating Secret '{}' in namespace '{}'.",
                request.getName(),
                request.getNamespace()
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

            io.fabric8.kubernetes.api.model.Secret existingSecret =
                    client.secrets()
                            .inNamespace(request.getNamespace())
                            .withName(request.getName())
                            .get();

            if (existingSecret != null) {

                throw new BadRequestException(
                        ErrorCode.SECRET_ALREADY_EXISTS,
                        "Secret already exists."
                );
            }

            io.fabric8.kubernetes.api.model.Secret secret =
                    buildSecret(request);

            io.fabric8.kubernetes.api.model.Secret createdSecret =
                    client.secrets()
                            .inNamespace(request.getNamespace())
                            .resource(secret)
                            .create();

            log.info(
                    "Secret '{}' created successfully.",
                    request.getName()
            );

            return secretMapper.toResponse(createdSecret);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create Secret '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.SECRET_CREATION_FAILED,
                    "Unable to create Secret."
            );
        }
    }

    @Override
    public List<SecretSummaryResponse> getSecrets(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Fetching Secrets for cluster '{}', namespace '{}'.",
                clusterId,
                namespace
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

            List<io.fabric8.kubernetes.api.model.Secret> secrets;

            if (namespace == null || namespace.isBlank()) {

                secrets = client.secrets()
                        .list()
                        .getItems();

            } else {

                secrets = client.secrets()
                        .inNamespace(namespace)
                        .list()
                        .getItems();
            }

            log.info(
                    "Found {} Secrets.",
                    secrets.size()
            );

            return secrets.stream()
                    .map(secretMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch Secrets.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch Secrets."
            );
        }
    }

    @Override
    public SecretResponse getSecret(
            UUID clusterId,
            String namespace,
            String secretName
    ) {

        log.info(
                "Fetching Secret '{}' from namespace '{}'.",
                secretName,
                namespace
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

            io.fabric8.kubernetes.api.model.Secret secret =
                    client.secrets()
                            .inNamespace(namespace)
                            .withName(secretName)
                            .get();

            if (secret == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.SECRET_NOT_FOUND,
                        "Secret not found."
                );
            }

            return secretMapper.toResponse(secret);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch Secret '{}'.",
                    secretName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch Secret."
            );
        }
    }

    @Override
    public void deleteSecret(
            UUID clusterId,
            String namespace,
            String secretName
    ) {

        log.info(
                "Deleting Secret '{}' from namespace '{}'.",
                secretName,
                namespace
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

            io.fabric8.kubernetes.api.model.Secret secret =
                    client.secrets()
                            .inNamespace(namespace)
                            .withName(secretName)
                            .get();

            if (secret == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.SECRET_NOT_FOUND,
                        "Secret not found."
                );
            }

            client.secrets()
                    .inNamespace(namespace)
                    .withName(secretName)
                    .delete();

            log.info(
                    "Secret '{}' deleted successfully.",
                    secretName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete Secret '{}'.",
                    secretName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to delete Secret."
            );
        }
    }



    private io.fabric8.kubernetes.api.model.Secret buildSecret(
            CreateSecretRequest request
    ) {

        String kubernetesType = switch (request.getType()) {

            case OPAQUE -> "Opaque";

            case TLS -> "kubernetes.io/tls";

            case DOCKER_CONFIG_JSON -> "kubernetes.io/dockerconfigjson";

            case BASIC_AUTH -> "kubernetes.io/basic-auth";

            case SSH_AUTH -> "kubernetes.io/ssh-auth";
        };

        return new SecretBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(request.getNamespace())
                .endMetadata()

                .withType(kubernetesType)

                .withStringData(
                        request.getData()
                )

                .build();
    }
}