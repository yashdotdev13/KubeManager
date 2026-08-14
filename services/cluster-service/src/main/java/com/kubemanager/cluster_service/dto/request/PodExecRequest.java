package com.kubemanager.cluster_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PodExecRequest {

    private String container;

    @NotEmpty(message = "Command is required.")
    private List<String> command;

}
