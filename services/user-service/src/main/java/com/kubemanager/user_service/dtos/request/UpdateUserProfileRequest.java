package com.kubemanager.user_service.dtos.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Size(max = 100)
    private String displayName;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 100)
    private String organization;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String jobTitle;

    @Size(max = 500)
    private String bio;

    @Size(max = 100)
    private String timezone;

}