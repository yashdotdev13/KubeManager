package com.kubemanager.user_service.dtos.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfoResponse {

    private String firstName;

    private String lastName;

    private String displayName;
}
