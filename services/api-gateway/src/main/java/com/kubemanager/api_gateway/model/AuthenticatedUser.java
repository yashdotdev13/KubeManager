package com.kubemanager.api_gateway.model;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticatedUser {

    private String userId;

    private String username;

    private String email;

    private List<String> roles;
}
