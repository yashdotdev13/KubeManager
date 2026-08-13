package com.kubemanager.cluster_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateJobRequest {

    @NotBlank(message = "Job name is required.")
    private String name;

    @NotBlank(message = "Container name is required.")
    private String containerName;

    @NotBlank(message = "Container image is required.")
    private String image;

    @NotNull(message = "Backoff limit is required.")
    @Min(value = 0, message = "Backoff limit cannot be negative.")
    private Integer backoffLimit;

    @NotNull(message = "Completions are required.")
    @Min(value = 1, message = "Completions must be at least 1.")
    private Integer completions;

    @NotNull(message = "Parallelism is required.")
    @Min(value = 1, message = "Parallelism must be at least 1.")
    private Integer parallelism;

    private Map<String, String> labels;

    private Map<String, String> environment;

    private String command;
}