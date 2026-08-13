package com.kubemanager.cluster_service.service.Impl;

import com.kubemanager.cluster_service.dto.request.CreateJobRequest;
import com.kubemanager.cluster_service.dto.response.JobResponse;
import com.kubemanager.cluster_service.dto.response.JobSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.JobMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.JobService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final JobMapper jobMapper;

    @Override
    public JobResponse createJob(
            UUID clusterId,
            String namespace,
            CreateJobRequest request
    ) {

        log.info(
                "Creating Job '{}' in namespace '{}' for cluster '{}'.",
                request.getName(),
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

            Job existingJob =
                    client.batch()
                            .v1()
                            .jobs()
                            .inNamespace(namespace)
                            .withName(request.getName())
                            .get();

            if (existingJob != null) {

                throw new BadRequestException(
                        ErrorCode.JOB_ALREADY_EXISTS,
                        "Job already exists."
                );
            }

            Job job = buildJob(namespace, request);

            Job createdJob =
                    client.batch()
                            .v1()
                            .jobs()
                            .inNamespace(namespace)
                            .resource(job)
                            .create();

            log.info(
                    "Job '{}' created successfully.",
                    request.getName()
            );

            return jobMapper.toResponse(createdJob);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create Job '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.JOB_CREATION_FAILED,
                    "Unable to create Job."
            );
        }
    }

    private Job buildJob(
            String namespace,
            CreateJobRequest request
    ) {

        ContainerBuilder containerBuilder =
                new ContainerBuilder()
                        .withName(request.getContainerName())
                        .withImage(request.getImage());

        /*
         * Convert command string into Kubernetes command.
         *
         * Example:
         * "echo Hello KubeManager"
         *
         * becomes:
         * ["sh", "-c", "echo Hello KubeManager"]
         */
        if (request.getCommand() != null &&
                !request.getCommand().isBlank()) {

            containerBuilder.withCommand(
                    "sh",
                    "-c",
                    request.getCommand()
            );
        }

        if (request.getEnvironment() != null &&
                !request.getEnvironment().isEmpty()) {

            request.getEnvironment()
                    .forEach((key, value) ->
                            containerBuilder
                                    .addNewEnv()
                                    .withName(key)
                                    .withValue(value)
                                    .endEnv()
                    );
        }

        Container container =
                containerBuilder.build();

        return new JobBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(namespace)
                .withLabels(request.getLabels())
                .endMetadata()

                .withNewSpec()

                .withBackoffLimit(
                        request.getBackoffLimit()
                )

                .withCompletions(
                        request.getCompletions()
                )

                .withParallelism(
                        request.getParallelism()
                )

                .withTemplate(
                        new PodTemplateSpecBuilder()

                                .withNewMetadata()
                                .withLabels(
                                        request.getLabels()
                                )
                                .endMetadata()

                                .withNewSpec()
                                .withRestartPolicy("Never")
                                .withContainers(container)
                                .endSpec()

                                .build()
                )

                .endSpec()

                .build();
    }

    @Override
    public List<JobSummaryResponse> getJobs(
            UUID clusterId,
            String namespace
    ) {
        return List.of();
    }

    @Override
    public JobResponse getJob(
            UUID clusterId,
            String namespace,
            String jobName
    ) {
        return null;
    }

    @Override
    public void deleteJob(
            UUID clusterId,
            String namespace,
            String jobName
    ) {
    }
}
