package com.kubemanager.auth_service.dto.response;

import com.kubemanager.auth_service.enums.AuthProvider;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;

    private String username;

    private String email;

    private AuthProvider provider;

    private String avatarUrl;

    private Set<String> roles;

}