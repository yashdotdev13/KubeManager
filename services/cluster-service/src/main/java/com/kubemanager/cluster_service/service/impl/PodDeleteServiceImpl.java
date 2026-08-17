package com.kubemanager.cluster_service.service.impl;

import com.kubemanager.cluster_service.dto.response.PodDeleteResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PodDeleteService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PodDeleteServiceImpl implements PodDeleteService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;

    @Override
    public PodDeleteResponse deletePod(
            UUID clusterId,
            String namespace,
            String podName
    ) {

        log.info(
                "Deleting pod '{}' in namespace '{}' for cluster '{}'.",
                podName,
                namespace,
                clusterId
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

            Pod pod = client.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .get();

            if (pod == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.POD_NOT_FOUND,
                        "Pod not found."
                );
            }

            List<StatusDetails> deleteResult = client.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .delete();

            if (deleteResult == null || deleteResult.isEmpty()) {

                throw new BadRequestException(
                        ErrorCode.POD_DELETE_FAILED,
                        "Failed to delete pod."
                );
            }

            log.info(
                    "Pod '{}' deleted successfully from namespace '{}'.",
                    podName,
                    namespace
            );

            return PodDeleteResponse.builder()
                    .podName(podName)
                    .namespace(namespace)
                    .status("DELETED")
                    .message(
                            "Pod '" +
                                    podName +
                                    "' deleted successfully."
                    )
                    .build();

        } catch (ResourceNotFoundException exception) {
            throw exception;

        } catch (BadRequestException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Failed to delete pod '{}'.",
                    podName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.POD_DELETE_FAILED,
                    "Unable to delete pod."
            );
        }
    }
}