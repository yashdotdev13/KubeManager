package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.CronJobResponse;
import com.kubemanager.cluster_service.dto.response.CronJobSummaryResponse;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.batch.v1.CronJob;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CronJobMapper {

    public CronJobResponse toResponse(CronJob cronJob) {

        String containerName = null;
        String image = null;
        Map<String, String> environment = null;

        if (cronJob.getSpec() != null &&
                cronJob.getSpec().getJobTemplate() != null &&
                cronJob.getSpec()
                        .getJobTemplate()
                        .getSpec() != null &&
                cronJob.getSpec()
                        .getJobTemplate()
                        .getSpec()
                        .getTemplate() != null &&
                cronJob.getSpec()
                        .getJobTemplate()
                        .getSpec()
                        .getTemplate()
                        .getSpec() != null &&
                cronJob.getSpec()
                        .getJobTemplate()
                        .getSpec()
                        .getTemplate()
                        .getSpec()
                        .getContainers() != null &&
                !cronJob.getSpec()
                        .getJobTemplate()
                        .getSpec()
                        .getTemplate()
                        .getSpec()
                        .getContainers()
                        .isEmpty()) {

            Container container =
                    cronJob.getSpec()
                            .getJobTemplate()
                            .getSpec()
                            .getTemplate()
                            .getSpec()
                            .getContainers()
                            .get(0);

            containerName = container.getName();
            image = container.getImage();

            if (container.getEnv() != null &&
                    !container.getEnv().isEmpty()) {

                environment = container.getEnv()
                        .stream()
                        .filter(env ->
                                env.getName() != null &&
                                        env.getValue() != null
                        )
                        .collect(Collectors.toMap(
                                env -> env.getName(),
                                env -> env.getValue()
                        ));
            }
        }

        Integer completions = null;
        Integer parallelism = null;
        Integer backoffLimit = null;

        if (cronJob.getSpec() != null &&
                cronJob.getSpec().getJobTemplate() != null &&
                cronJob.getSpec().getJobTemplate().getSpec() != null) {

            completions =
                    cronJob.getSpec()
                            .getJobTemplate()
                            .getSpec()
                            .getCompletions();

            parallelism =
                    cronJob.getSpec()
                            .getJobTemplate()
                            .getSpec()
                            .getParallelism();

            backoffLimit =
                    cronJob.getSpec()
                            .getJobTemplate()
                            .getSpec()
                            .getBackoffLimit();
        }

        OffsetDateTime creationTimestamp = null;

        if (cronJob.getMetadata() != null &&
                cronJob.getMetadata().getCreationTimestamp() != null) {

            creationTimestamp = OffsetDateTime.parse(
                    cronJob.getMetadata().getCreationTimestamp()
            );
        }

        return CronJobResponse.builder()

                .name(
                        cronJob.getMetadata().getName()
                )

                .namespace(
                        cronJob.getMetadata().getNamespace()
                )

                .schedule(
                        cronJob.getSpec() != null
                                ? cronJob.getSpec().getSchedule()
                                : null
                )

                .completions(
                        completions
                )

                .parallelism(
                        parallelism
                )

                .backoffLimit(
                        backoffLimit
                )

                .containerName(
                        containerName
                )

                .image(
                        image
                )

                .labels(
                        cronJob.getMetadata().getLabels()
                )

                .environment(
                        environment
                )

                .creationTimestamp(
                        creationTimestamp
                )

                .suspend(
                        cronJob.getSpec() != null
                                ? cronJob.getSpec().getSuspend()
                                : null
                )

                .successfulJobsHistoryLimit(
                        cronJob.getSpec() != null
                                ? cronJob.getSpec()
                                .getSuccessfulJobsHistoryLimit()
                                : null
                )

                .failedJobsHistoryLimit(
                        cronJob.getSpec() != null
                                ? cronJob.getSpec()
                                .getFailedJobsHistoryLimit()
                                : null
                )

                .build();
    }

    public CronJobSummaryResponse toSummaryResponse(
            CronJob cronJob
    ) {

        String lastScheduleTime = null;

        if (cronJob.getStatus() != null &&
                cronJob.getStatus().getLastScheduleTime() != null) {

            lastScheduleTime =
                    cronJob.getStatus().getLastScheduleTime();
        }

        return CronJobSummaryResponse.builder()

                .name(
                        cronJob.getMetadata().getName()
                )

                .schedule(
                        cronJob.getSpec() != null
                                ? cronJob.getSpec().getSchedule()
                                : null
                )

                .suspend(
                        cronJob.getSpec() != null
                                ? cronJob.getSpec().getSuspend()
                                : null
                )

                .lastScheduleTime(
                        lastScheduleTime
                )

                .status(
                        determineStatus(cronJob)
                )

                .build();
    }

    private String determineStatus(CronJob cronJob) {

        if (cronJob.getSpec() != null &&
                Boolean.TRUE.equals(
                        cronJob.getSpec().getSuspend()
                )) {

            return "SUSPENDED";
        }

        if (cronJob.getStatus() != null &&
                cronJob.getStatus().getLastScheduleTime() != null) {

            return "SCHEDULED";
        }

        return "ACTIVE";
    }
}