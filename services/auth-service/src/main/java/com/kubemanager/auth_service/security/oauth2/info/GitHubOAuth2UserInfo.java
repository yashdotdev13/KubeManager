package com.kubemanager.auth_service.security.oauth2.info;


import com.kubemanager.auth_service.security.oauth2.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GitHubOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getUsername() {
        return (String) attributes.get("login");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getAvatarUrl() {
        return (String) attributes.get("avatar_url");
    }
}