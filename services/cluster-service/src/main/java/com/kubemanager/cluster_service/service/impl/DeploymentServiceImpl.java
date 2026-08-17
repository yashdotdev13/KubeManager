package com.kubemanager.cluster_service.service.impl;

import com.kubemanager.cluster_service.dto.request.CreateDeploymentRequest;
import com.kubemanager.cluster_service.dto.request.ScaleDeploymentRequest;
import com.kubemanager.cluster_service.dto.response.DeploymentResponse;
import com.kubemanager.cluster_service.dto.response.DeploymentSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.DeploymentMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.DeploymentService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class DeploymentServiceImpl implements DeploymentService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final DeploymentMapper deploymentMapper;


    @Override
    public List<DeploymentSummaryResponse> getDeployments(UUID clusterId, String namespace) {


        log.info("Fetching deployments for cluster '{}' and aamespace '{}'",
                clusterId, namespace);

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(()-> new ResourceNotFoundException(ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found"));

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

            DeploymentList deploymentList;

            if (namespace == null || namespace.trim().isEmpty()) {

                deploymentList = client.apps()
                        .deployments()
                        .inAnyNamespace()
                        .list();

            } else {

                deploymentList = client.apps()
                        .deployments()
                        .inNamespace(namespace.trim())
                        .list();
            }

            log.info(
                    "Found {} deployments.",
                    deploymentList.getItems().size()
            );

            return deploymentList.getItems()
                    .stream()
                    .map(deploymentMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch deployments.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch deployments."
            );
        }
    }

    @Override
    public DeploymentResponse getDeployment(UUID clusterId, String namespace, String deploymentName) {


        log.info("Fetching deployment '{}' in namespace '{}' for cluster '{}'",
                deploymentName, namespace, clusterId);

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found"
                ));

        if(cluster.getEncryptedKubeConfig()== null || cluster.getEncryptedKubeConfig().isBlank()){
            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster Kubeconfig is not available."
            );
        }
        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {

            Deployment deployment = client.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .get();

            if (deployment == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.DEPLOYMENT_NOT_FOUND,
                        "Deployment not found."
                );
            }

            log.info(
                    "Successfully fetched deployment '{}'",
                    deploymentName
            );

            return deploymentMapper.toResponse(deployment);

        } catch (ResourceNotFoundException exception) {
            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch deployment '{}'",
                    deploymentName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch deployment."
            );
        }
    }

    @Override
    public DeploymentResponse scaleDeployment(UUID clusterId, String namespace, String deploymentName, ScaleDeploymentRequest request) {


        log.info("Scaling deployment '{}' namespace '{}' to {} replicas",
                deploymentName,
                namespace,
                request.getReplicas());

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(()->new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found"
                ));

        if(cluster.getEncryptedKubeConfig() == null ||
        cluster.getEncryptedKubeConfig().isBlank()){

    throw new BadRequestException(
            ErrorCode.INVALID_CLUSTER_CONFIGURATION,
            "Cluster Kubeconfig is not available."
    );
        }

        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {

            Deployment deployment = client.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .get();

            if (deployment == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.DEPLOYMENT_NOT_FOUND,
                        "Deployment not found."
                );
            }

            deployment.getSpec().setReplicas(request.getReplicas());

            Deployment updatedDeployment = client.apps()
                    .deployments()
                    .resource(deployment)
                    .update();

            log.info(
                    "Deployment '{}' scaled successfully to {} replicas.",
                    deploymentName,
                    request.getReplicas()
            );

            return deploymentMapper.toResponse(updatedDeployment);

        } catch (ResourceNotFoundException exception) {
            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to scale deployment '{}'.",
                    deploymentName,
                    exception
            );
            throw new BadRequestException(
                    ErrorCode.DEPLOYMENT_SCALE_FAILED,
                    "Unable to scale deployment."
            );
        }
    }

    @Override
    public void restartDeployment(UUID clusterId, String namespace, String deploymentName) {


        log.info("Restarting deployment '{}' in namespace '{}'",deploymentName, namespace);

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

            Deployment deployment = client.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .get();

            if (deployment == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.DEPLOYMENT_NOT_FOUND,
                        "Deployment not found."
                );
            }

            if (deployment.getSpec().getTemplate().getMetadata().getAnnotations() == null) {

                deployment.getSpec()
                        .getTemplate()
                        .getMetadata()
                        .setAnnotations(new HashMap<>());
            }

            deployment.getSpec()
                    .getTemplate()
                    .getMetadata()
                    .getAnnotations()
                    .put(
                            "kubectl.kubernetes.io/restartedAt",
                            Instant.now().toString()
                    );

            client.apps()
                    .deployments()
                    .resource(deployment)
                    .update();

            log.info(
                    "Deployment '{}' restarted successfully.",
                    deploymentName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to restart deployment '{}'.",
                    deploymentName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.DEPLOYMENT_RESTART_FAILED,
                    "Unable to restart deployment."
            );
        }

    }

    @Override
    public void deleteDeployment(UUID clusterId, String namespace, String deploymentName) {

        log.info("Deleting deployment '{}' from namespace '{}'",deploymentName,
                namespace);

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

            Deployment deployment = client.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .get();

            if (deployment == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.DEPLOYMENT_NOT_FOUND,
                        "Deployment not found."
                );
            }

            boolean deleted = client.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .withName(deploymentName)
                    .delete()
                    .size() > 0;

            if (!deleted) {

                throw new BadRequestException(
                        ErrorCode.DEPLOYMENT_DELETE_FAILED,
                        "Failed to delete deployment."
                );
            }

            log.info(
                    "Deployment '{}' deleted successfully.",
                    deploymentName
            );

        } catch (ResourceNotFoundException | BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete deployment '{}'.",
                    deploymentName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to delete deployment."
            );
        }

    }

    @Override
    public DeploymentResponse createDeployment(UUID clusterId, CreateDeploymentRequest request) {


        log.info("Creating deployment '{}' in namespace '{}'",
                request.getName(), request.getNamespace());

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(()->new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found"
                ));
        if(cluster.getEncryptedKubeConfig()== null ||
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

            Deployment existingDeployment = client.apps()
                    .deployments()
                    .inNamespace(request.getNamespace())
                    .withName(request.getName())
                    .get();

            if (existingDeployment != null) {

                throw new BadRequestException(
                        ErrorCode.DEPLOYMENT_ALREADY_EXISTS,
                        "Deployment already exists."
                );
            }
            Deployment deployment = buildDeployment(request);

            Deployment createdDeployment = client.apps()
                    .deployments()
                    .inNamespace(request.getNamespace())
                    .resource(deployment)
                    .create();

            log.info(
                    "Deployment '{}' created successfully.",
                    request.getName()
            );

            return deploymentMapper.toResponse(createdDeployment);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create deployment '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.DEPLOYMENT_CREATION_FAILED,
                    "Unable to create deployment."
            );
        }
    }


    private Deployment buildDeployment(
            CreateDeploymentRequest request
    ) {

        return new DeploymentBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(request.getNamespace())
                .addToLabels("app", request.getName())
                .endMetadata()

                .withNewSpec()

                .withReplicas(request.getReplicas())

                .withNewSelector()
                .addToMatchLabels("app", request.getName())
                .endSelector()

                .withNewTemplate()

                .withNewMetadata()
                .addToLabels("app", request.getName())
                .endMetadata()

                .withNewSpec()

                .addNewContainer()

                .withName(request.getName())

                .withImage(request.getImage())

                .addNewPort()
                .withContainerPort(
                        request.getContainerPort()
                )
                .endPort()

                .endContainer()

                .endSpec()

                .endTemplate()

                .endSpec()

                .build();
    }
}
