package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.JobResponse;
import com.kubemanager.cluster_service.dto.response.JobSummaryResponse;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JobMapper {

    public JobResponse toResponse(Job job) {

        String containerName = null;
        String image = null;
        Map<String, String> environment = null;

        if (job.getSpec() != null &&
                job.getSpec().getTemplate() != null &&
                job.getSpec().getTemplate().getSpec() != null &&
                job.getSpec().getTemplate().getSpec().getContainers() != null &&
                !job.getSpec().getTemplate().getSpec().getContainers().isEmpty()) {

            Container container =
                    job.getSpec()
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

        OffsetDateTime creationTimestamp = null;

        if (job.getMetadata() != null &&
                job.getMetadata().getCreationTimestamp() != null) {

            creationTimestamp = OffsetDateTime.parse(
                    job.getMetadata().getCreationTimestamp()
            );
        }

        Integer completions = null;
        Integer parallelism = null;
        Integer backoffLimit = null;

        if (job.getSpec() != null) {

            completions = job.getSpec().getCompletions();
            parallelism = job.getSpec().getParallelism();
            backoffLimit = job.getSpec().getBackoffLimit();
        }

        Integer succeeded = null;
        Integer failed = null;
        Integer active = null;

        if (job.getStatus() != null) {

            succeeded = job.getStatus().getSucceeded();
            failed = job.getStatus().getFailed();
            active = job.getStatus().getActive();
        }

        return JobResponse.builder()

                .name(
                        job.getMetadata().getName()
                )

                .namespace(
                        job.getMetadata().getNamespace()
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

                .succeeded(
                        succeeded
                )

                .failed(
                        failed
                )

                .active(
                        active
                )

                .containerName(
                        containerName
                )

                .image(
                        image
                )

                .labels(
                        job.getMetadata().getLabels()
                )

                .environment(
                        environment
                )

                .creationTimestamp(
                        creationTimestamp
                )

                .build();
    }

    public JobSummaryResponse toSummaryResponse(Job job) {

        Integer completions = null;
        Integer succeeded = null;
        Integer failed = null;
        Integer active = null;

        if (job.getSpec() != null) {

            completions = job.getSpec().getCompletions();
        }

        if (job.getStatus() != null) {

            succeeded = job.getStatus().getSucceeded();
            failed = job.getStatus().getFailed();
            active = job.getStatus().getActive();
        }

        String status = determineStatus(
                active,
                succeeded,
                failed,
                completions
        );

        return JobSummaryResponse.builder()

                .name(
                        job.getMetadata().getName()
                )

                .completions(
                        completions
                )

                .succeeded(
                        succeeded
                )

                .failed(
                        failed
                )

                .active(
                        active
                )

                .status(
                        status
                )

                .build();
    }

    private String determineStatus(
            Integer active,
            Integer succeeded,
            Integer failed,
            Integer completions
    ) {

        if (succeeded != null &&
                completions != null &&
                succeeded >= completions) {

            return "COMPLETED";
        }

        if (failed != null && failed > 0) {
            return "FAILED";
        }

        if (active != null && active > 0) {
            return "RUNNING";
        }

        return "PENDING";
    }
}