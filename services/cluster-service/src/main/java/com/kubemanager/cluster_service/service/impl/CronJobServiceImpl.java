package com.kubemanager.cluster_service.service.impl;

import com.kubemanager.cluster_service.dto.request.CreateCronJobRequest;
import com.kubemanager.cluster_service.dto.response.CronJobResponse;
import com.kubemanager.cluster_service.dto.response.CronJobSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.mapper.CronJobMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.CronJobService;

import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import io.fabric8.kubernetes.api.model.batch.v1.CronJobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobTemplateSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CronJobServiceImpl implements CronJobService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final CronJobMapper cronJobMapper;


    @Override
    public CronJobResponse createCronJob(
            UUID clusterId,
            String namespace,
            CreateCronJobRequest request
    ) {

        log.info(
                "Creating CronJob '{}' in namespace '{}' for cluster '{}'.",
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

            CronJob existingCronJob =
                    client.batch()
                            .v1()
                            .cronjobs()
                            .inNamespace(namespace)
                            .withName(request.getName())
                            .get();

            if (existingCronJob != null) {

                throw new BadRequestException(
                        ErrorCode.CRON_JOB_ALREADY_EXISTS,
                        "CronJob already exists."
                );
            }

            CronJob cronJob = buildCronJob(namespace, request);

            CronJob createdCronJob =
                    client.batch()
                            .v1()
                            .cronjobs()
                            .inNamespace(namespace)
                            .resource(cronJob)
                            .create();

            log.info(
                    "CronJob '{}' created successfully.",
                    request.getName()
            );

            return cronJobMapper.toResponse(createdCronJob);

        } catch (BadRequestException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to create CronJob '{}'.",
                    request.getName(),
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.CRON_JOB_CREATION_FAILED,
                    "Unable to create CronJob."
            );
        }
    }

    @Override
    public List<CronJobSummaryResponse> getCronJobs(
            UUID clusterId,
            String namespace
    ) {

        log.info(
                "Fetching CronJobs in namespace '{}' for cluster '{}'.",
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

            List<CronJob> cronJobs =
                    client.batch()
                            .v1()
                            .cronjobs()
                            .inNamespace(namespace)
                            .list()
                            .getItems();

            log.info(
                    "Found {} CronJob(s) in namespace '{}'.",
                    cronJobs.size(),
                    namespace
            );

            return cronJobs.stream()
                    .map(cronJobMapper::toSummaryResponse)
                    .toList();

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch CronJobs in namespace '{}'.",
                    namespace,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.CRON_JOB_NOT_FOUND,
                    "Unable to fetch CronJobs."
            );
        }
    }

    @Override
    public CronJobResponse getCronJob(
            UUID clusterId,
            String namespace,
            String cronJobName
    ) {

        log.info(
                "Fetching CronJob '{}' in namespace '{}' for cluster '{}'.",
                cronJobName,
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

            CronJob cronJob =
                    client.batch()
                            .v1()
                            .cronjobs()
                            .inNamespace(namespace)
                            .withName(cronJobName)
                            .get();

            if (cronJob == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.CRON_JOB_NOT_FOUND,
                        "CronJob not found."
                );
            }

            log.info(
                    "CronJob '{}' fetched successfully.",
                    cronJobName
            );

            return cronJobMapper.toResponse(cronJob);

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to fetch CronJob '{}'.",
                    cronJobName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.CRON_JOB_NOT_FOUND,
                    "Unable to fetch CronJob."
            );
        }
    }

    @Override
    public void deleteCronJob(
            UUID clusterId,
            String namespace,
            String cronJobName
    ) {

        log.info(
                "Deleting CronJob '{}' in namespace '{}' for cluster '{}'.",
                cronJobName,
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

            CronJob cronJob =
                    client.batch()
                            .v1()
                            .cronjobs()
                            .inNamespace(namespace)
                            .withName(cronJobName)
                            .get();

            if (cronJob == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.CRON_JOB_NOT_FOUND,
                        "CronJob not found."
                );
            }

            client.batch()
                    .v1()
                    .cronjobs()
                    .inNamespace(namespace)
                    .withName(cronJobName)
                    .delete();

            log.info(
                    "CronJob '{}' deleted successfully.",
                    cronJobName
            );

        } catch (ResourceNotFoundException exception) {

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Failed to delete CronJob '{}'.",
                    cronJobName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.CRON_JOB_DELETION_FAILED,
                    "Unable to delete CronJob."
            );
        }
    }


    private CronJob buildCronJob(
            String namespace,
            CreateCronJobRequest request
    ) {

        ContainerBuilder containerBuilder =
                new ContainerBuilder()
                        .withName(request.getContainerName())
                        .withImage(request.getImage());

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

        Container container = containerBuilder.build();

        return new CronJobBuilder()

                .withNewMetadata()
                .withName(request.getName())
                .withNamespace(namespace)
                .withLabels(request.getLabels())
                .endMetadata()

                .withNewSpec()

                .withSchedule(request.getSchedule())

                .withSuspend(false)

                .withSuccessfulJobsHistoryLimit(3)

                .withFailedJobsHistoryLimit(1)

                .withJobTemplate(
                        new JobTemplateSpecBuilder()

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
                                                .withRestartPolicy(
                                                        "Never"
                                                )
                                                .withContainers(
                                                        container
                                                )
                                                .endSpec()

                                                .build()
                                )

                                .endSpec()

                                .build()
                )

                .endSpec()

                .build();
    }
}
