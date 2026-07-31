package com.kubemanager.api_gateway.filter;

import com.kubemanager.api_gateway.model.AuthenticatedUser;
import com.kubemanager.api_gateway.security.JwtClaims;
import com.kubemanager.api_gateway.security.JwtConstants;
import com.kubemanager.api_gateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String authorizationHeader =
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(JwtConstants.BEARER)) {

            return chain.filter(exchange);
        }

        String token = authorizationHeader.substring(JwtConstants.BEARER.length());

        if (!jwtService.validate(token)) {
            return chain.filter(exchange);
        }

        JwtClaims claims = jwtService.extractClaims(token);

        List<SimpleGrantedAuthority> authorities =
                claims.getRoles() == null
                        ? Collections.emptyList()
                        : claims.getRoles()
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        AuthenticatedUser principal = AuthenticatedUser.builder()
                .userId(claims.getUserId())
                .username(claims.getUsername())
                .email(claims.getEmail())
                .roles(claims.getRoles())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        return chain.filter(exchange)
                .contextWrite(
                        ReactiveSecurityContextHolder.withAuthentication(authentication)
                );
    }
}