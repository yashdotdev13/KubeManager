package com.kubemanager.ai_service.client;


import com.kubemanager.ai_service.auth.HeaderConstants;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import com.kubemanager.ai_service.dto.ClusterHealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClusterServiceClient {

    private static final String CLUSTER_SERVICE =
            "http://CLUSTER-SERVICE";

    private final RestClient.Builder restClientBuilder;

    public ClusterHealthResponse healthCheck(UUID clusterId) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        return restClientBuilder
                .build()
                .post()
                .uri(
                        CLUSTER_SERVICE
                                + "/api/v1/clusters/{clusterId}/health-check",
                        clusterId
                )
                .headers(headers -> {

                    addHeader(
                            headers,
                            HeaderConstants.USER_ID,
                            context.getUserId()
                    );

                    addHeader(
                            headers,
                            HeaderConstants.USERNAME,
                            context.getUsername()
                    );

                    addHeader(
                            headers,
                            HeaderConstants.EMAIL,
                            context.getEmail()
                    );

                    addHeader(
                            headers,
                            HeaderConstants.ROLES,
                            context.getRoles() == null
                                    ? null
                                    : String.join(
                                    ",",
                                    context.getRoles()
                            )
                    );

                    addHeader(
                            headers,
                            HeaderConstants.PROVIDER,
                            context.getProvider()
                    );

                    addHeader(
                            headers,
                            HeaderConstants.REQUEST_ID,
                            context.getRequestId()
                    );

                    addHeader(
                            headers,
                            HeaderConstants.CORRELATION_ID,
                            context.getCorrelationId()
                    );
                })
                .retrieve()
                .body(ClusterHealthResponse.class);
    }

    private void addHeader(
            org.springframework.http.HttpHeaders headers,
            String name,
            Object value
    ) {

        if (value != null) {
            headers.set(name, value.toString());
        }
    }
}