package com.kubemanager.cluster_service.service.Impl;

import com.kubemanager.cluster_service.dto.response.PodLogsResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PodLogsService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PodLogsServiceImpl implements PodLogsService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;


    @Override
    public PodLogsResponse getPodLogs(
            UUID clusterId,
            String namespace,
            String podName,
            String container,
            Integer tailLines,
            Boolean previous
    ) {

        log.info(
                "Fetching logs for pod '{}' in namespace '{}' for cluster '{}'.",
                podName,
                namespace,
                clusterId
        );


        /*
         * 1. Find cluster
         */
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));


        /*
         * 2. Validate kubeconfig
         */
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


            /*
             * 3. Find Pod
             */
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


            /*
             * 4. Validate containers
             */
            if (pod.getSpec() == null ||
                    pod.getSpec().getContainers() == null ||
                    pod.getSpec().getContainers().isEmpty()) {

                throw new BadRequestException(
                        ErrorCode.POD_LOGS_UNAVAILABLE,
                        "Pod does not contain any containers."
                );
            }


            /*
             * 5. Select container
             *
             * If container is not provided,
             * use the first container.
             */
            String selectedContainer = container;

            if (selectedContainer == null ||
                    selectedContainer.isBlank()) {

                selectedContainer = pod.getSpec()
                        .getContainers()
                        .get(0)
                        .getName();
            }


            /*
             * 6. Validate requested container
             */
            String finalSelectedContainer = selectedContainer;
            boolean containerExists = pod.getSpec()
                    .getContainers()
                    .stream()
                    .anyMatch(c ->
                            finalSelectedContainer.equals(c.getName())
                    );

            if (!containerExists) {

                throw new BadRequestException(
                        ErrorCode.POD_LOGS_UNAVAILABLE,
                        "Container '" +
                                selectedContainer +
                                "' not found in pod."
                );
            }


            /*
             * 7. Build Pod resource
             */
            var podResource = client.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .inContainer(selectedContainer);


            String logs;


            /*
             * 8. Previous container logs
             */
            if (Boolean.TRUE.equals(previous)) {

                /*
                 * Fabric8 exposes previous/terminated
                 * container logs through terminated().
                 */
                if (tailLines != null && tailLines > 0) {

                    logs = podResource
                            .terminated()
                            .tailingLines(tailLines)
                            .getLog();

                } else {

                    logs = podResource
                            .terminated()
                            .getLog();
                }

            }


            /*
             * 9. Current container logs
             */
            else {

                if (tailLines != null && tailLines > 0) {

                    logs = podResource
                            .tailingLines(tailLines)
                            .getLog();

                } else {

                    logs = podResource
                            .getLog();
                }
            }


            /*
             * 10. Return response
             */
            return PodLogsResponse.builder()
                    .podName(podName)
                    .namespace(namespace)
                    .containerName(selectedContainer)
                    .logs(logs)
                    .build();


        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch logs for pod '{}', container '{}'.",
                    podName,
                    selectedContainerSafe(container),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.POD_LOGS_UNAVAILABLE,
                    "Unable to fetch pod logs."
            );
        }
    }


    /*
     * Prevent logging a null/blank container awkwardly.
     */
    private String selectedContainerSafe(String container) {

        if (container == null || container.isBlank()) {
            return "default";
        }

        return container;
    }
}