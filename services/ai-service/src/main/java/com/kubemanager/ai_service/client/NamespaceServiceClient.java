package com.kubemanager.ai_service.client;

import com.kubemanager.ai_service.auth.HeaderConstants;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import com.kubemanager.ai_service.dto.NamespaceServiceApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NamespaceServiceClient {

    private final RestClient restClient;

    @Value("${kubemanager.services.cluster-service.url}")
    private String clusterServiceUrl;

    public NamespaceServiceApiResponse getNamespaces(UUID clusterId) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        NamespaceServiceApiResponse response =
                restClient
                        .get()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/namespaces",
                                clusterId
                        )
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .retrieve()
                        .body(NamespaceServiceApiResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "Empty response received from cluster-service."
            );
        }
        if (!response.isSuccess()) {
            throw new IllegalStateException(
                    response.getMessage()
            );
        }

        return response;
    }

    private void addUserContextHeaders(
            HttpHeaders headers,
            UserContext context
    ) {

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
                        : String.join(",", context.getRoles())
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
    }

    private void addHeader(
            HttpHeaders headers,
            String name,
            Object value
    ) {

        if (value != null) {
            headers.set(
                    name,
                    value.toString()
            );
        }
    }
}
