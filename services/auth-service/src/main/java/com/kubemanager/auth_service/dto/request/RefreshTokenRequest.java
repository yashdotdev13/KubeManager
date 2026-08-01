package com.kubemanager.auth_service.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshTokenRequest {


    @NotBlank
    private String refreshToken;
}
