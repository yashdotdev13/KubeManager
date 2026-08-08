package com.kubemanager.cluster_service.service.Impl;


import com.kubemanager.cluster_service.dto.response.PodResponse;
import com.kubemanager.cluster_service.dto.response.PodSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.PodMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PodService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PodServiceImpl implements PodService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final PodMapper podMapper;


    @Override
    public List<PodSummaryResponse> getPods(UUID clusterId, String namespace) {


        log.info("Fetching pods for cluster '{}' and namespace '{}'",clusterId, namespace);

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(()->new ResourceNotFoundException(ErrorCode.CLUSTER_NOT_FOUND,
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

            PodList podList;

            if (namespace == null || namespace.isBlank()) {

                podList = client.pods().inAnyNamespace().list();

            } else {

                podList = client.pods()
                        .inNamespace(namespace)
                        .list();
            }
            log.info(
                    "Found {} pods.",
                    podList.getItems().size()
            );

            return podList.getItems()
                    .stream()
                    .map(podMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch pods.",
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Unable to fetch pods."
            );
        }
    }

    @Override
    public PodResponse getPod(UUID clusterId, String namespace, String podName) {
        return null;
    }

    @Override
    public void deletePod(UUID clusterId, String namespace, String podName) {

    }
}
