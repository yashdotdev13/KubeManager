package com.kubemanager.cluster_service.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateConfigMapRequest {

    private String name;

    private String namespace;

    private Map<String, String> data;
}
