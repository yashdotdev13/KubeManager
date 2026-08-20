package com.kubemanager.ai_service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.auth.HeaderConstants;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import com.kubemanager.ai_service.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClusterServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${kubemanager.services.cluster-service.url}")
    private String clusterServiceUrl;

    public ClusterHealthResponse healthCheck(UUID clusterId) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        ClusterServiceApiResponse response =
                restClient
                        .post()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/health-check",
                                clusterId
                        )
                        .headers(headers ->
                                addUserContextHeaders(headers, context)
                        )
                        .retrieve()
                        .body(ClusterServiceApiResponse.class);

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

        return response.getData();
    }

    public NamespaceResponse getNamespace(
            UUID clusterId,
            String namespace
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        NamespaceInfoServiceApiResponse response =
                restClient
                        .get()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/namespaces/{namespace}",
                                clusterId,
                                namespace
                        )
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .retrieve()
                        .body(NamespaceInfoServiceApiResponse.class);

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

        return response.getData();
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

    public NamespaceApiResponse createNamespace(
            UUID clusterId,
            String name
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        CreateNamespaceRequest request =
                CreateNamespaceRequest.builder()
                        .name(name)
                        .build();

        NamespaceApiResponse response =
                restClient
                        .post()
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
                        .body(request)
                        .retrieve()
                        .body(NamespaceApiResponse.class);

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

    public void deleteNamespace(
            UUID clusterId,
            String namespace
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        ClusterServiceApiResponse response =
                restClient
                        .delete()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/namespaces/{namespace}",
                                clusterId,
                                namespace
                        )
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .retrieve()
                        .body(ClusterServiceApiResponse.class);

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
    }


    public PodListServiceApiResponse getPods(
            UUID clusterId,
            String namespace
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        PodListServiceApiResponse response =
                restClient
                        .get()
                        .uri(uriBuilder -> {

                            var builder = uriBuilder
                                    .path(
                                            clusterServiceUrl
                                                    + "/api/v1/clusters/{clusterId}/pods"
                                    )
                                    .queryParam("namespace", namespace);

                            return builder.build(clusterId);
                        })
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .retrieve()
                        .body(PodListServiceApiResponse.class);

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


    public PodInfoServiceApiResponse getPod(
            UUID clusterId,
            String namespace,
            String podName
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        PodInfoServiceApiResponse response =
                restClient
                        .get()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/pods/{namespace}/{podName}",
                                clusterId,
                                namespace,
                                podName
                        )
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .retrieve()
                        .body(PodInfoServiceApiResponse.class);

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