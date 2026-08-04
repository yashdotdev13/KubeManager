package com.kubemanager.user_service.mapper;

import com.kubemanager.user_service.dtos.request.CreateUserProfileRequest;
import com.kubemanager.user_service.dtos.request.UpdateUserProfileRequest;
import com.kubemanager.user_service.dtos.response.ContactInfoResponse;
import com.kubemanager.user_service.dtos.response.PersonalInfoResponse;
import com.kubemanager.user_service.dtos.response.ProfessionalInfoResponse;
import com.kubemanager.user_service.dtos.response.UserProfileResponse;
import com.kubemanager.user_service.entity.UserProfile;
import com.kubemanager.user_service.entity.embeddable.ContactInfo;
import com.kubemanager.user_service.entity.embeddable.PersonalInfo;
import com.kubemanager.user_service.entity.embeddable.ProfessionalInfo;
import org.springframework.stereotype.Component;

@Component
public class UserProfileMapper {

    public UserProfile toEntity(CreateUserProfileRequest request) {

        return UserProfile.builder()

                .personalInfo(
                        PersonalInfo.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .displayName(request.getDisplayName())
                                .build()
                )

                .contactInfo(
                        ContactInfo.builder()
                                .phoneNumber(request.getPhoneNumber())
                                .build()
                )

                .professionalInfo(
                        ProfessionalInfo.builder()
                                .organization(request.getOrganization())
                                .department(request.getDepartment())
                                .jobTitle(request.getJobTitle())
                                .build()
                )

                .bio(request.getBio())
                .timezone(request.getTimezone())

                .build();
    }

    public void updateEntity(
            UserProfile profile,
            UpdateUserProfileRequest request
    ) {

        if (profile.getPersonalInfo() == null) {
            profile.setPersonalInfo(new PersonalInfo());
        }

        if (profile.getProfessionalInfo() == null) {
            profile.setProfessionalInfo(new ProfessionalInfo());
        }

        if (profile.getContactInfo() == null) {
            profile.setContactInfo(new ContactInfo());
        }

        profile.getPersonalInfo().setFirstName(request.getFirstName());
        profile.getPersonalInfo().setLastName(request.getLastName());
        profile.getPersonalInfo().setDisplayName(request.getDisplayName());

        profile.getProfessionalInfo().setOrganization(request.getOrganization());
        profile.getProfessionalInfo().setDepartment(request.getDepartment());
        profile.getProfessionalInfo().setJobTitle(request.getJobTitle());

        profile.getContactInfo().setPhoneNumber(request.getPhoneNumber());

        profile.setBio(request.getBio());
        profile.setTimezone(request.getTimezone());
    }

    public UserProfileResponse toResponse(UserProfile profile) {

        return UserProfileResponse.builder()

                .userId(profile.getUserId())

                .personalInfo(
                        PersonalInfoResponse.builder()
                                .firstName(profile.getPersonalInfo().getFirstName())
                                .lastName(profile.getPersonalInfo().getLastName())
                                .displayName(profile.getPersonalInfo().getDisplayName())
                                .build()
                )

                .contactInfo(
                        ContactInfoResponse.builder()
                                .phoneNumber(profile.getContactInfo().getPhoneNumber())
                                .build()
                )

                .professionalInfo(
                        ProfessionalInfoResponse.builder()
                                .organization(profile.getProfessionalInfo().getOrganization())
                                .department(profile.getProfessionalInfo().getDepartment())
                                .jobTitle(profile.getProfessionalInfo().getJobTitle())
                                .build()
                )

                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .visibility(profile.getVisibility())
                .status(profile.getStatus())
                .language(profile.getLanguage())
                .timezone(profile.getTimezone())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())

                .build();
    }

}