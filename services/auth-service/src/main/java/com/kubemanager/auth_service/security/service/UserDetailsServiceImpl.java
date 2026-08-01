package com.kubemanager.auth_service.security.service;

import com.kubemanager.auth_service.entity.User;
import com.kubemanager.auth_service.security.model.CustomUserDetails;
import com.kubemanager.auth_service.service.UserService;
import com.kubemanager.exception.ResourceNotFoundException;
import com.kubemanager.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        log.debug("Loading user details for '{}'.", username);

        User user = userService.findByUsername(username)
                .orElseThrow(() -> {

                    log.warn("User '{}' not found.", username);

                    return new ResourceNotFoundException(
                            ErrorCode.USER_NOT_FOUND,
                            "User not found."
                    );
                });

        return new CustomUserDetails(user);
    }

}