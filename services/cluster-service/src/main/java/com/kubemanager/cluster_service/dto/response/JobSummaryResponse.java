package com.kubemanager.cluster_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobSummaryResponse {

    private String name;

    private Integer completions;

    private Integer succeeded;

    private Integer failed;

    private Integer active;

    private String status;
}