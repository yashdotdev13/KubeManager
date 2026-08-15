package com.kubemanager.cluster_service.service.Impl;

import com.kubemanager.cluster_service.dto.response.PodRestartResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PodRestartService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.OwnerReference;
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
public class PodRestartServiceImpl implements PodRestartService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;

    @Override
    public PodRestartResponse restartPod(
            UUID clusterId,
            String namespace,
            String podName
    ) {

        log.info(
                "Restarting pod '{}' in namespace '{}' for cluster '{}'.",
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

            if (pod.getMetadata() == null) {

                throw new BadRequestException(
                        ErrorCode.POD_RESTART_FAILED,
                        "Pod metadata is not available."
                );
            }

            List<OwnerReference> owners =
                    pod.getMetadata().getOwnerReferences();

            if (owners == null || owners.isEmpty()) {

                throw new BadRequestException(
                        ErrorCode.POD_RESTART_FAILED,
                        "Pod is not managed by a controller and cannot be safely restarted."
                );
            }

            OwnerReference controllerOwner = owners.stream()
                    .filter(owner ->
                            Boolean.TRUE.equals(owner.getController())
                    )
                    .findFirst()
                    .orElse(null);

            if (controllerOwner == null) {

                throw new BadRequestException(
                        ErrorCode.POD_RESTART_FAILED,
                        "Pod does not have a controller owner."
                );
            }

            log.info(
                    "Pod '{}' is managed by {} '{}'.",
                    podName,
                    controllerOwner.getKind(),
                    controllerOwner.getName()
            );

            List<StatusDetails> deleteResult = client.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .delete();

            if (deleteResult == null || deleteResult.isEmpty()) {

                throw new BadRequestException(
                        ErrorCode.POD_RESTART_FAILED,
                        "Failed to delete pod for restart."
                );
            }

            log.info(
                    "Pod '{}' deleted successfully. Waiting for controller '{}' to recreate it.",
                    podName,
                    controllerOwner.getName()
            );

            return PodRestartResponse.builder()
                    .podName(podName)
                    .namespace(namespace)
                    .status("RESTARTING")
                    .message(
                            "Pod restart initiated. Kubernetes controller "
                                    + controllerOwner.getKind()
                                    + " '"
                                    + controllerOwner.getName()
                                    + "' will create a replacement pod."
                    )
                    .build();

        } catch (ResourceNotFoundException exception) {
            throw exception;

        } catch (BadRequestException exception) {
            throw exception;

        } catch (Exception exception) {
            log.error(
                    "Failed to restart pod '{}'.",
                    podName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.POD_RESTART_FAILED,
                    "Unable to restart pod."
            );
        }
    }
}