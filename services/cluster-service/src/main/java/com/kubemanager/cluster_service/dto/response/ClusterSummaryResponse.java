package com.kubemanager.cluster_service.dto.response;

import com.kubemanager.cluster_service.enums.ClusterProvider;
import com.kubemanager.cluster_service.enums.ClusterStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterSummaryResponse {

    private UUID id;

    private String name;

    private ClusterProvider provider;

    private ClusterStatus status;
}
