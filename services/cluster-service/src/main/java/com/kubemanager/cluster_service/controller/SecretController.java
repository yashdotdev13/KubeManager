package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateSecretRequest;
import com.kubemanager.cluster_service.dto.response.SecretResponse;
import com.kubemanager.cluster_service.dto.response.SecretSummaryResponse;
import com.kubemanager.cluster_service.service.SecretService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters/{clusterId}/secrets")
public class SecretController {

    private final SecretService secretService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SecretResponse> createSecret(

            @PathVariable UUID clusterId,

            @Valid
            @RequestBody
            CreateSecretRequest request
    ) {

        return ApiResponse.success(
                "Secret created successfully.",
                secretService.createSecret(
                        clusterId,
                        request
                )
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<SecretSummaryResponse>> getSecrets(

            @PathVariable UUID clusterId,

            @RequestParam(required = false)
            String namespace
    ) {

        return ApiResponse.success(
                "Secrets fetched successfully.",
                secretService.getSecrets(
                        clusterId,
                        namespace
                )
        );
    }

    @GetMapping("/{namespace}/{secretName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<SecretResponse> getSecret(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String secretName
    ) {

        return ApiResponse.success(
                "Secret fetched successfully.",
                secretService.getSecret(
                        clusterId,
                        namespace,
                        secretName
                )
        );
    }

    @DeleteMapping("/{namespace}/{secretName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteSecret(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String secretName
    ) {

        secretService.deleteSecret(
                clusterId,
                namespace,
                secretName
        );

        return ApiResponse.success(
                "Secret deleted successfully.",
                null
        );
    }
}