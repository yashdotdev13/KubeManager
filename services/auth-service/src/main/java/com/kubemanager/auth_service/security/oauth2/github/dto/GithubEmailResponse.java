package com.kubemanager.auth_service.security.oauth2.github.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubEmailResponse {



    private String email;

    private boolean primary;

    private boolean verified;

    @JsonProperty("visibility")
    private String visibility;
}
