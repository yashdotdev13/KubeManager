package com.kubemanager.api_gateway.security;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JwtClaims {

    private String userId;
    private String username;
    private String email;
    private List<String> roles;

}