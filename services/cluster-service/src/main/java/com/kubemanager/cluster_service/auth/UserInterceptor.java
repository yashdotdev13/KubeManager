package com.kubemanager.cluster_service.auth;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {

        String userIdHeader =
                request.getHeader(HeaderConstants.USER_ID);

        if (userIdHeader == null || userIdHeader.isBlank()) {

            log.warn(
                    "Missing '{}' header for request '{} {}'",
                    HeaderConstants.USER_ID,
                    request.getMethod(),
                    request.getRequestURI()
            );

            return true;
        }

        List<String> roles =
                request.getHeader(HeaderConstants.ROLES) == null
                        ? List.of()
                        : Arrays.stream(
                                request.getHeader(HeaderConstants.ROLES)
                                        .split(",")
                        )
                        .map(String::trim)
                        .toList();

        UserContext context =
                UserContext.builder()
                        .userId(UUID.fromString(userIdHeader))
                        .username(request.getHeader(HeaderConstants.USERNAME))
                        .email(request.getHeader(HeaderConstants.EMAIL))
                        .roles(roles)
                        .provider(request.getHeader(HeaderConstants.PROVIDER))
                        .requestId(request.getHeader(HeaderConstants.REQUEST_ID))
                        .correlationId(request.getHeader(HeaderConstants.CORRELATION_ID))
                        .build();

        UserContextHolder.set(context);

        log.debug(
                "User context initialized for '{}' ({})",
                context.getUsername(),
                context.getUserId()
        );

        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex
    ) {

        UserContextHolder.clear();

        log.debug(
                "User context cleared for request '{} {}'",
                request.getMethod(),
                request.getRequestURI()
        );
    }
}