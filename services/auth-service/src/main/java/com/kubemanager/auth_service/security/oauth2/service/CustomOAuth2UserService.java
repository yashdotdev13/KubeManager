package com.kubemanager.auth_service.security.oauth2.service;


import com.kubemanager.auth_service.security.oauth2.info.GitHubOAuth2UserInfo;
import com.kubemanager.auth_service.security.oauth2.info.OAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo;

        switch (registrationId.toLowerCase()) {

            case "github" ->
                    userInfo = new GitHubOAuth2UserInfo(
                            oauth2User.getAttributes()
                    );

            default ->
                    throw new OAuth2AuthenticationException(
                            "Unsupported OAuth2 Provider: " + registrationId
                    );
        }

        log.info(
                "OAuth2 login request received from '{}' for '{}'.",
                registrationId,
                userInfo.getUsername()
        );

        /*
         * For now we simply return the OAuth2User.
         *
         * In the next step we will:
         * 1. Find existing user.
         * 2. Create user if first login.
         * 3. Update avatar.
         * 4. Save provider/providerId.
         */

        return oauth2User;
    }
}