package com.kubemanager.api_gateway.util;


import com.kubemanager.api_gateway.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Mono<AuthenticatedUser> getCurrentUser() {

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .map(Authentication::getPrincipal)
                .cast(AuthenticatedUser.class);
    }

}