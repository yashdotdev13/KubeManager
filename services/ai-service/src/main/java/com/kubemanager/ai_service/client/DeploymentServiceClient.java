package com.kubemanager.ai_service.client;

import com.kubemanager.ai_service.auth.HeaderConstants;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import com.kubemanager.ai_service.dto.DeploymentInfoServiceApiResponse;
import com.kubemanager.ai_service.dto.DeploymentScaleRequest;
import com.kubemanager.ai_service.dto.DeploymentScaleServiceApiResponse;
import com.kubemanager.ai_service.dto.DeploymentServiceApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeploymentServiceClient {

    private final RestClient restClient;

    @Value("${kubemanager.services.cluster-service.url}")
    private String clusterServiceUrl;

    public DeploymentServiceApiResponse getDeployments(
            UUID clusterId
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        DeploymentServiceApiResponse response =
                restClient
                        .get()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/deployments",
                                clusterId
                        )
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .retrieve()
                        .body(DeploymentServiceApiResponse.class);

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

    public DeploymentServiceApiResponse getDeploymentsByNamespace(
            UUID clusterId,
            String namespace
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        DeploymentServiceApiResponse response =
                restClient
                        .get()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/deployments/{namespace}",
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
                        .body(DeploymentServiceApiResponse.class);

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


    public DeploymentInfoServiceApiResponse getDeployment(
            UUID clusterId,
            String namespace,
            String deploymentName
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        DeploymentInfoServiceApiResponse response =
                restClient
                        .get()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/deployments/{namespace}/{deploymentName}",
                                clusterId,
                                namespace,
                                deploymentName
                        )
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .retrieve()
                        .body(DeploymentInfoServiceApiResponse.class);

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


    public DeploymentScaleServiceApiResponse scaleDeployment(
            UUID clusterId,
            String namespace,
            String deploymentName,
            Integer replicas
    ) {

        UserContext context =
                UserContextHolder.getRequiredContext();

        DeploymentScaleRequest request =
                DeploymentScaleRequest.builder()
                        .replicas(replicas)
                        .build();

        DeploymentScaleServiceApiResponse response =
                restClient
                        .put()
                        .uri(
                                clusterServiceUrl
                                        + "/api/v1/clusters/{clusterId}/deployments/{namespace}/{deploymentName}/scale",
                                clusterId,
                                namespace,
                                deploymentName
                        )
                        .headers(headers ->
                                addUserContextHeaders(
                                        headers,
                                        context
                                )
                        )
                        .body(request)
                        .retrieve()
                        .body(DeploymentScaleServiceApiResponse.class);

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