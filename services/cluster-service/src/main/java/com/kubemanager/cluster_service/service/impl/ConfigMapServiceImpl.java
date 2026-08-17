package com.kubemanager.cluster_service.service.impl;

import com.kubemanager.cluster_service.dto.request.CreateConfigMapRequest;
import com.kubemanager.cluster_service.dto.response.ConfigMapResponse;
import com.kubemanager.cluster_service.dto.response.ConfigMapSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.ConfigMapMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.ConfigMapService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;



@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigMapServiceImpl implements ConfigMapService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final ConfigMapMapper configMapMapper;

    @Override
    public ConfigMapResponse createConfigMap(UUID clusterId, CreateConfigMapRequest request) {


        log.info("Creating ConfigMaps '{}' in namespace '{}'",request.getName(),
                request.getNamespace());

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(()->new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found"
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

            io.fabric8.kubernetes.api.model.ConfigMap existingConfigMap =
                    client.configMaps()
                            .inNamespace(request.getNamespace())
                            .withName(request.getName())
                            .get();

            if (existingConfigMap != null) {

                throw new BadRequestException(
                        ErrorCode.CONFIG_MAP_ALREADY_EXISTS,
                        "ConfigMap already exists."
                );
            }

            io.fabric8.kubernetes.api.model.ConfigMap configMap =
                    buildConfigMap(request);

            io.fabric8.kubernetes.api.model.ConfigMap createdConfigMap =
                    client.configMaps()
                            .inNamespace(request.getNamespace())
                            .resource(configMap)
                            .create();

            log.info(
                    "ConfigMap '{}' created successfully.",
                    request.getName()
            );

            return configMapMapper.toResponse(createdConfigMap);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create ConfigMap '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.CONFIG_MAP_CREATION_FAILED,
                    "Unable to create ConfigMap."
            );
        }
    }

    @Override
    public List<ConfigMapSummaryResponse> getConfigMaps(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Fetching ConfigMaps for cluster '{}', namespace '{}'.",
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

            List<io.fabric8.kubernetes.api.model.ConfigMap> configMaps;

            if (namespace == null || namespace.isBlank()) {

                configMaps = client.configMaps()
                        .list()
                        .getItems();

            } else {

                configMaps = client.configMaps()
                        .inNamespace(namespace)
                        .list()
                        .getItems();
            }

            log.info(
                    "Found {} ConfigMaps.",
                    configMaps.size()
            );

            return configMaps.stream()
                    .map(configMapMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch ConfigMaps.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch ConfigMaps."
            );
        }
    }

    @Override
    public ConfigMapResponse getConfigMap(
            UUID clusterId,
            String namespace,
            String configMapName
    ) {

        log.info(
                "Fetching ConfigMap '{}' from namespace '{}'.",
                configMapName,
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

            io.fabric8.kubernetes.api.model.ConfigMap configMap =
                    client.configMaps()
                            .inNamespace(namespace)
                            .withName(configMapName)
                            .get();

            if (configMap == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.CONFIG_MAP_NOT_FOUND,
                        "ConfigMap not found."
                );
            }

            return configMapMapper.toResponse(configMap);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch ConfigMap '{}'.",
                    configMapName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch ConfigMap."
            );
        }
    }

    @Override
    public void deleteConfigMap(
            UUID clusterId,
            String namespace,
            String configMapName
    ) {

        log.info(
                "Deleting ConfigMap '{}' from namespace '{}'.",
                configMapName,
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

            io.fabric8.kubernetes.api.model.ConfigMap configMap =
                    client.configMaps()
                            .inNamespace(namespace)
                            .withName(configMapName)
                            .get();

            if (configMap == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.CONFIG_MAP_NOT_FOUND,
                        "ConfigMap not found."
                );
            }

            client.configMaps()
                    .inNamespace(namespace)
                    .withName(configMapName)
                    .delete();

            log.info(
                    "ConfigMap '{}' deleted successfully.",
                    configMapName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete ConfigMap '{}'.",
                    configMapName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to delete ConfigMap."
            );
        }
    }

    private io.fabric8.kubernetes.api.model.ConfigMap buildConfigMap(
            CreateConfigMapRequest request
    ) {

        return new ConfigMapBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(request.getNamespace())
                .endMetadata()

                .withData(
                        request.getData()
                )

                .build();
    }
}
