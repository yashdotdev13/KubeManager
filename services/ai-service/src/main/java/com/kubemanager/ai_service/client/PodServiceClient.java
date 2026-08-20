package com.kubemanager.ai_service.client;


import com.kubemanager.ai_service.auth.HeaderConstants;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import com.kubemanager.ai_service.dto.PodDeleteServiceApiResponse;
import com.kubemanager.ai_service.dto.PodInfoServiceApiResponse;
import com.kubemanager.ai_service.dto.PodServiceApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PodServiceClient {

    private final RestClient restClient;

    @Value("${kubemanager.services.cluster-service.url}")
    private String clusterServiceUrl;

    public PodServiceApiResponse getPods(
            UUID clusterId,
            String namespace
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        PodServiceApiResponse response =
                restClient
                        .get()
                        .uri(uriBuilder ->
                                uriBuilder
                                        .path(
                                                clusterServiceUrl
                                                        + "/api/v1/clusters/{clusterId}/pods"
                                        )
                                        .queryParam(
                                                "namespace",
                                                namespace
                                        )
                                        .build(clusterId)
                        )
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .retrieve()
                        .body(PodServiceApiResponse.class);

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


    public void deletePod(
            UUID clusterId,
            String namespace,
            String podName
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        PodDeleteServiceApiResponse response =
                restClient
                        .delete()
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
                        .body(PodDeleteServiceApiResponse.class);

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