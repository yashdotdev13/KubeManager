package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.response.SecretResponse;
import com.kubemanager.cluster_service.dto.response.SecretSummaryResponse;
import io.fabric8.kubernetes.api.model.Secret;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class SecretMapper {

    public SecretSummaryResponse toSummaryResponse(
            Secret secret
    ) {

        return SecretSummaryResponse.builder()
                .name(
                        secret.getMetadata().getName()
                )
                .namespace(
                        secret.getMetadata().getNamespace()
                )
                .type(
                        secret.getType()
                )
                .dataEntries(
                        getDataEntries(secret)
                )
                .build();
    }

    public SecretResponse toResponse(
            Secret secret
    ) {

        return SecretResponse.builder()
                .name(
                        secret.getMetadata().getName()
                )
                .namespace(
                        secret.getMetadata().getNamespace()
                )
                .type(
                        secret.getType()
                )
                .data(
                        getStringData(secret)
                )
                .creationTimestamp(
                        secret.getMetadata().getCreationTimestamp() != null
                                ? OffsetDateTime.parse(
                                secret.getMetadata().getCreationTimestamp()
                        )
                                : null
                )
                .build();
    }

    private Integer getDataEntries(
            Secret secret
    ) {

        return secret.getData() != null
                ? secret.getData().size()
                : 0;
    }

    private Map<String, String> getStringData(
            Secret secret
    ) {

        Map<String, String> data = new HashMap<>();

        if (secret.getData() == null) {
            return data;
        }

        secret.getData().forEach((key, value) -> {
            byte[] decoded =
                    java.util.Base64.getDecoder().decode(value);

            data.put(
                    key,
                    new String(decoded)
            );
        });

        return data;
    }
}