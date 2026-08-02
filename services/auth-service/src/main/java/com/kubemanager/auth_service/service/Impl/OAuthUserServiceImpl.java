package com.kubemanager.auth_service.service.Impl;


import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.enums.AuthProvider;
import com.kubemanager.auth_service.repository.RoleRepository;
import com.kubemanager.auth_service.repository.UserRepository;
import com.kubemanager.auth_service.security.oauth2.info.OAuth2UserInfo;
import com.kubemanager.auth_service.service.OAuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OAuthUserServiceImpl implements OAuthUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    @Override
    public User processOauth2UserInfo(String registrationId, OAuth2UserInfo userInfo) {


        log.info("Processing OAuth2 user '{}' from provider '{}'",userInfo.getEmail(),
                registrationId);

        Optional<User> providerUser = userRepository.findByProviderAndProviderId(
                AuthProvider.GITHUB, userInfo.getProviderId()
        );

        if(providerUser.isPresent()){
            log.info("Existing OAuth user '{}' found",userInfo.getEmail());

            return providerUser.get();
        }

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());

        if(existingUser.isPresent()){

            User user = existingUser.get();

            user.setProvider(AuthProvider.GITHUB);
            user.setProviderId(userInfo.getProviderId());
            user.setAvatarUrl(userInfo.getAvatarUrl());

            log.info("Linked existing LOCAL account '{}' with Github",
                    user.getEmail());

            return userRepository.save(user);

    }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_USER not found."));

        User user = User.builder()
                .username(userInfo.getUsername())
                .email(userInfo.getEmail())
                .provider(AuthProvider.GITHUB)
                .providerId(userInfo.getProviderId())
                .avatarUrl(userInfo.getAvatarUrl())
                .enabled(true)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);

        log.info(
                "Created new OAuth user '{}'.",
                savedUser.getEmail()
        );

        return savedUser;
    }
}
