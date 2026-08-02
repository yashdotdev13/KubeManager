package com.kubemanager.auth_service.security.oauth2.github.client;



import com.kubemanager.auth_service.security.oauth2.github.dto.GithubEmailResponse;

import java.util.List;

public interface GitHubApiClient {

    List<GithubEmailResponse> getUserEmails(String accessToken);

}