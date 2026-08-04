package com.kubemanager.user_service.dtos.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalInfoResponse {

    private String organization;

    private String department;

    private String jobTitle;

}