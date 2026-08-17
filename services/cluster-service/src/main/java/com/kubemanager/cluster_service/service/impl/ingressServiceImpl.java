package com.kubemanager.cluster_service.service.impl;


import com.kubemanager.cluster_service.dto.request.CreateIngressRequest;
import com.kubemanager.cluster_service.dto.response.IngressResponse;
import com.kubemanager.cluster_service.dto.response.IngressSummaryResponse;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.IngressMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.IngressService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import com.kubemanager.cluster_service.entity.Cluster;

import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ingressServiceImpl implements IngressService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final IngressMapper ingressMapper;


    @Override
    public IngressResponse createIngress(
            UUID clusterId,
            CreateIngressRequest request
    ) {

        log.info(
                "Creating ingress '{}' in namespace '{}'.",
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

            io.fabric8.kubernetes.api.model.networking.v1.Ingress existingIngress =
                    client.network()
                            .v1()
                            .ingresses()
                            .inNamespace(request.getNamespace())
                            .withName(request.getName())
                            .get();

            if (existingIngress != null) {

                throw new BadRequestException(
                        ErrorCode.INGRESS_ALREADY_EXISTS,
                        "Ingress already exists."
                );
            }

            io.fabric8.kubernetes.api.model.networking.v1.Ingress ingress =
                    buildIngress(request);

            io.fabric8.kubernetes.api.model.networking.v1.Ingress createdIngress =
                    client.network()
                            .v1()
                            .ingresses()
                            .inNamespace(request.getNamespace())
                            .resource(ingress)
                            .create();

            log.info(
                    "Ingress '{}' created successfully.",
                    request.getName()
            );

            return ingressMapper.toResponse(createdIngress);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create ingress '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INGRESS_CREATION_FAILED,
                    "Unable to create ingress."
            );
        }
    }

    @Override
    public List<IngressSummaryResponse> getIngresses(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Fetching ingresses for cluster '{}', namespace '{}'.",
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

            List<io.fabric8.kubernetes.api.model.networking.v1.Ingress> ingresses;

            if (namespace == null || namespace.isBlank()) {

                ingresses = client.network()
                        .v1()
                        .ingresses()
                        .list()
                        .getItems();

            } else {

                ingresses = client.network()
                        .v1()
                        .ingresses()
                        .inNamespace(namespace)
                        .list()
                        .getItems();
            }

            log.info(
                    "Found {} ingresses.",
                    ingresses.size()
            );

            return ingresses.stream()
                    .map(ingressMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch ingresses.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch ingresses."
            );
        }
    }

    @Override
    public IngressResponse getIngress(
            UUID clusterId,
            String namespace,
            String ingressName
    ) {

        log.info(
                "Fetching ingress '{}' from namespace '{}'.",
                ingressName,
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

            io.fabric8.kubernetes.api.model.networking.v1.Ingress ingress =
                    client.network()
                            .v1()
                            .ingresses()
                            .inNamespace(namespace)
                            .withName(ingressName)
                            .get();

            if (ingress == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.INGRESS_NOT_FOUND,
                        "Ingress not found."
                );
            }

            return ingressMapper.toResponse(ingress);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch ingress '{}'.",
                    ingressName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch ingress."
            );
        }
    }

    @Override
    public void deleteIngress(
            UUID clusterId,
            String namespace,
            String ingressName
    ) {

        log.info(
                "Deleting ingress '{}' from namespace '{}'.",
                ingressName,
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

            io.fabric8.kubernetes.api.model.networking.v1.Ingress ingress =
                    client.network()
                            .v1()
                            .ingresses()
                            .inNamespace(namespace)
                            .withName(ingressName)
                            .get();

            if (ingress == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.INGRESS_NOT_FOUND,
                        "Ingress not found."
                );
            }

            client.network()
                    .v1()
                    .ingresses()
                    .inNamespace(namespace)
                    .withName(ingressName)
                    .delete();

            log.info(
                    "Ingress '{}' deleted successfully.",
                    ingressName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;
        } catch (Exception exception) {

            log.error(
                    "Failed to delete ingress '{}'.",
                    ingressName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to delete ingress."
            );
        }
    }

    private io.fabric8.kubernetes.api.model.networking.v1.Ingress buildIngress(
            CreateIngressRequest request
    ) {

        return new IngressBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(request.getNamespace())
                .endMetadata()
                .withNewSpec()
                .addNewRule()
                .withHost(request.getHost())
                .withNewHttp()
                .addNewPath()
                .withPath(request.getPath())
                .withPathType("Prefix")
                .withNewBackend()
                .withNewService()
                .withName(request.getServiceName())
                .withNewPort()
                .withNumber(request.getServicePort())
                .endPort()
                .endService()
                .endBackend()
                .endPath()
                .endHttp()
                .endRule()
                .endSpec()
                .build();
    }
}
