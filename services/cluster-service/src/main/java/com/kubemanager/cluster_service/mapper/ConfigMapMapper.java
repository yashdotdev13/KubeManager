package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.ConfigMapResponse;
import com.kubemanager.cluster_service.dto.response.ConfigMapSummaryResponse;
import io.fabric8.kubernetes.api.model.ConfigMap;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class ConfigMapMapper {

    public ConfigMapSummaryResponse toSummaryResponse(
            ConfigMap configMap
    ) {

        return ConfigMapSummaryResponse.builder()
                .name(
                        configMap.getMetadata().getName()
                )
                .namespace(
                        configMap.getMetadata().getNamespace()
                )
                .dataEntries(
                        getDataEntries(configMap)
                )
                .build();
    }

    public ConfigMapResponse toResponse(
            ConfigMap configMap
    ) {

        return ConfigMapResponse.builder()
                .name(
                        configMap.getMetadata().getName()
                )
                .namespace(
                        configMap.getMetadata().getNamespace()
                )
                .data(
                        configMap.getData()
                )
                .creationTimestamp(
                        configMap.getMetadata().getCreationTimestamp() != null
                                ? OffsetDateTime.parse(
                                configMap.getMetadata().getCreationTimestamp()
                        )
                                : null
                )
                .build();
    }

    private Integer getDataEntries(
            ConfigMap configMap
    ) {

        return configMap.getData() != null
                ? configMap.getData().size()
                : 0;
    }
}