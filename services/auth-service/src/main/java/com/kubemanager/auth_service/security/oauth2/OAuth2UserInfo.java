package com.kubemanager.auth_service.security.oauth2;


public interface OAuth2UserInfo {

    String getProviderId();

    String getUsername();

    String getEmail();

    String getAvatarUrl();

}