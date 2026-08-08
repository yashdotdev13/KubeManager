package com.kubemanager.cluster_service.service.Impl;

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
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        return null;
    }

    @Override
    public DeploymentResponse scaleDeployment(UUID clusterId, String namespace, String deploymentName, ScaleDeploymentRequest request) {
        return null;
    }

    @Override
    public void restartDeployment(UUID clusterId, String namespace, String deploymentName) {

    }

    @Override
    public void deleteDeployment(UUID clusterId, String namespace, String deploymentName) {

    }
}
