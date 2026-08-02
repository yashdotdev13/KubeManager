package com.kubemanager.auth_service.security.oauth2.service;

import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.security.oauth2.github.client.GitHubApiClient;
import com.kubemanager.auth_service.security.oauth2.github.dto.GithubEmailResponse;
import com.kubemanager.auth_service.security.oauth2.info.GitHubOAuth2UserInfo;
import com.kubemanager.auth_service.security.oauth2.info.OAuth2UserInfo;
import com.kubemanager.auth_service.security.oauth2.user.OAuth2UserPrincipal;
import com.kubemanager.auth_service.service.OAuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuthUserService oauthUserService;
    private final GitHubApiClient gitHubApiClient;

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(
            OAuth2UserRequest userRequest
    ) throws OAuth2AuthenticationException {

        OAuth2User oauth2User =
                delegate.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration()
                        .getRegistrationId();

        Map<String, Object> attributes =
                new HashMap<>(oauth2User.getAttributes());


        if ("github".equalsIgnoreCase(registrationId)
                && attributes.get("email") == null) {

            String accessToken =
                    userRequest.getAccessToken().getTokenValue();

            List<GithubEmailResponse> emails =
                    gitHubApiClient.getUserEmails(accessToken);

            emails.stream()
                    .filter(GithubEmailResponse::isPrimary)
                    .filter(GithubEmailResponse::isVerified)
                    .findFirst()
                    .ifPresent(email ->
                            attributes.put("email", email.getEmail())
                    );
        }

        log.info("========================================");
        log.info("GitHub User Attributes");
        attributes.forEach((key, value) ->
                log.info("{} -> {}", key, value));
        log.info("========================================");

        OAuth2UserInfo userInfo;

        switch (registrationId.toLowerCase()) {

            case "github" ->
                    userInfo = new GitHubOAuth2UserInfo(attributes);

            default ->
                    throw new OAuth2AuthenticationException(
                            "Unsupported OAuth2 Provider: "
                                    + registrationId
                    );
        }

        log.info(
                "OAuth2 login request received from provider '{}' for user '{}'.",
                registrationId,
                userInfo.getEmail()
        );

        User user = oauthUserService.processOauth2UserInfo(
                registrationId,
                userInfo
        );

        log.info(
                "OAuth2 authentication completed successfully for '{}'.",
                user.getUsername()
        );

        return new OAuth2UserPrincipal(
                user,
                attributes,
                oauth2User.getAuthorities()
        );
    }
}