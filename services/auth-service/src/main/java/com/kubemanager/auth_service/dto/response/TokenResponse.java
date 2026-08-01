package com.kubemanager.auth_service.dto.response;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType;
}
