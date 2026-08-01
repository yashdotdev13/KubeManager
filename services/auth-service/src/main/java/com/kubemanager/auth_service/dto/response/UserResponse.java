package com.kubemanager.auth_service.dto.response;


import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private UUID id;

    private String username;

    private String email;

    private Set<String> roles;
}
