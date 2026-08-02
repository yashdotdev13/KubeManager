package com.kubemanager.auth_service.service;

import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.security.oauth2.info.OAuth2UserInfo;

public interface OAuthUserService {

    User processOauth2UserInfo(String registrationId,
                               OAuth2UserInfo userInfo);
}
