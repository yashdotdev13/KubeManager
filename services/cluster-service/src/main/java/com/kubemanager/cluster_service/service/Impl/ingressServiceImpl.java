package com.kubemanager.cluster_service.service.Impl;


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

import com.kubemanager.cluster_service.dto.request.CreateIngressRequest;
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
    public List<IngressSummaryResponse> getIngresses(UUID clusterId, String namespace) {
        return List.of();
    }

    @Override
    public IngressResponse getIngress(UUID clusterId, String namespace) {
        return null;
    }

    @Override
    public void deleteIngress(UUID clusterId, String namespace, String ingressName) {

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
