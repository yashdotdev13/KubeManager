package com.kubemanager.auth_service.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogoutRequest {

    @NotBlank
    private String refreshToken;
}
