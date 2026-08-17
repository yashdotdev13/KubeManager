package com.kubemanager.cluster_service.service.impl;


import com.kubemanager.cluster_service.dto.request.CreateServiceRequest;
import com.kubemanager.cluster_service.dto.response.ServiceResponse;
import com.kubemanager.cluster_service.dto.response.ServiceSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.ServiceMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.ServiceService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final ServiceMapper serviceMapper;


    @Override
    public ServiceResponse createService(
            UUID clusterId,
            CreateServiceRequest request
    ) {

        log.info(
                "Creating service '{}' in namespace '{}'.",
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

            io.fabric8.kubernetes.api.model.Service existingService =
                    client.services()
                            .inNamespace(request.getNamespace())
                            .withName(request.getName())
                            .get();

            if (existingService != null) {

                throw new BadRequestException(
                        ErrorCode.SERVICE_ALREADY_EXISTS,
                        "Service already exists."
                );
            }

            io.fabric8.kubernetes.api.model.Service service =
                    buildService(request);

            io.fabric8.kubernetes.api.model.Service createdService =
                    client.services()
                            .inNamespace(request.getNamespace())
                            .resource(service)
                            .create();

            log.info(
                    "Service '{}' created successfully.",
                    request.getName()
            );

            return serviceMapper.toResponse(createdService);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create service '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.SERVICE_CREATION_FAILED,
                    "Unable to create service."
            );
        }
    }

    @Override
    public List<ServiceSummaryResponse> getServices(UUID clusterId, String namespace) {


        log.info("Fetching services for cluster '{}', namesoace '{}'",
                clusterId, namespace);

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(()-> new ResourceNotFoundException(
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

            List<io.fabric8.kubernetes.api.model.Service> services;

            if (namespace == null || namespace.isBlank()) {

                services = client.services()
                        .list()
                        .getItems();

            } else {

                services = client.services()
                        .inNamespace(namespace)
                        .list()
                        .getItems();
            }

            log.info(
                    "Found {} services.",
                    services.size()
            );

            return services.stream()
                    .map(serviceMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch services.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch services."
            );
        }
    }

    @Override
    public ServiceResponse getService(
            UUID clusterId,
            String namespace,
            String serviceName
    ) {

        log.info(
                "Fetching service '{}' from namespace '{}'.",
                serviceName,
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

            io.fabric8.kubernetes.api.model.Service service =
                    client.services()
                            .inNamespace(namespace)
                            .withName(serviceName)
                            .get();

            if (service == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.SERVICE_NOT_FOUND,
                        "Service not found."
                );
            }

            return serviceMapper.toResponse(service);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch service '{}'.",
                    serviceName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch service."
            );
        }
    }

    @Override
    public void deleteService(
            UUID clusterId,
            String namespace,
            String serviceName
    ) {

        log.info(
                "Deleting service '{}' from namespace '{}'.",
                serviceName,
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

            io.fabric8.kubernetes.api.model.Service service =
                    client.services()
                            .inNamespace(namespace)
                            .withName(serviceName)
                            .get();

            if (service == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.SERVICE_NOT_FOUND,
                        "Service not found."
                );
            }

            client.services()
                    .inNamespace(namespace)
                    .withName(serviceName)
                    .delete();

            log.info(
                    "Service '{}' deleted successfully.",
                    serviceName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete service '{}'.",
                    serviceName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to delete service."
            );
        }
    }


    private io.fabric8.kubernetes.api.model.Service buildService(
            CreateServiceRequest request
    ) {

        String kubernetesType = switch (request.getType()) {
            case CLUSTER_IP -> "ClusterIP";
            case NODE_PORT -> "NodePort";
            case LOAD_BALANCER -> "LoadBalancer";
        };

        return new ServiceBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(request.getNamespace())
                .endMetadata()

                .withNewSpec()

                .withType(kubernetesType)

                .withSelector(request.getSelector())

                .addNewPort()

                .withPort(request.getPort())

                .withTargetPort(
                        new IntOrString(
                                request.getTargetPort()
                        )
                )

                .endPort()

                .endSpec()

                .build();
    }
}
