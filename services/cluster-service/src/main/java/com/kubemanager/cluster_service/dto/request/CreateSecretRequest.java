package com.kubemanager.cluster_service.dto.request;


import com.kubemanager.cluster_service.enums.SecretType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSecretRequest {

    private String name;

    private String namespace;

    private SecretType type;

    private Map<String, String> data;
}
