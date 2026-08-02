package com.kubemanager.auth_service.security.oauth2.github.client.Impl;


import com.kubemanager.auth_service.security.oauth2.github.client.GitHubApiClient;
import com.kubemanager.auth_service.security.oauth2.github.dto.GithubEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubApiClientImpl implements GitHubApiClient {

    private static final String GITHUB_EMAIL_API =
            "https://api.github.com/user/emails";

    private final RestClient restClient;

    @Override
    public List<GithubEmailResponse> getUserEmails(String accessToken) {

        log.info("Fetching GitHub user emails.");

        List<GithubEmailResponse> response =
                restClient.get()

                        .uri(GITHUB_EMAIL_API)

                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + accessToken
                        )

                        .retrieve()

                        .body(new ParameterizedTypeReference<>() {
                        });

        return response == null ? List.of() : response;
    }
}
