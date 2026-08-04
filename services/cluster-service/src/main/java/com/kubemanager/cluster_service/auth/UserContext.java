package com.kubemanager.cluster_service.auth;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserContext {

    private UUID userId;

    private String username;

    private String email;

    private List<String> roles;

    private String provider;

    private String requestId;

    private String correlationId;
}
