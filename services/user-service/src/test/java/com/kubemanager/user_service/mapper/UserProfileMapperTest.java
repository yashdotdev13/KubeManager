package com.kubemanager.user_service.mapper;

import com.kubemanager.user_service.dtos.request.CreateUserProfileRequest;
import com.kubemanager.user_service.dtos.request.UpdateUserProfileRequest;
import com.kubemanager.user_service.dtos.response.UserProfileResponse;
import com.kubemanager.user_service.entity.UserProfile;
import com.kubemanager.user_service.enums.Language;
import com.kubemanager.user_service.enums.ProfileVisibility;
import com.kubemanager.user_service.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileMapperTest {

    private UserProfileMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserProfileMapper();
    }

    @Test
    void toEntity_shouldMapCreateRequestFields() {
        CreateUserProfileRequest request =
                CreateUserProfileRequest.builder()
                        .firstName("Yash")
                        .lastName("Sharma")
                        .displayName("Yash S")
                        .phoneNumber("9876543210")
                        .organization("KubeManager")
                        .department("Engineering")
                        .jobTitle("Backend Engineer")
                        .bio("Building distributed systems")
                        .timezone("Asia/Kolkata")
                        .build();

        UserProfile profile = mapper.toEntity(request);

        assertNotNull(profile);
        assertNull(profile.getUserId());

        assertEquals("Yash", profile.getPersonalInfo().getFirstName());
        assertEquals("Sharma", profile.getPersonalInfo().getLastName());
        assertEquals("Yash S", profile.getPersonalInfo().getDisplayName());

        assertEquals("9876543210", profile.getContactInfo().getPhoneNumber());

        assertEquals(
                "KubeManager",
                profile.getProfessionalInfo().getOrganization()
        );
        assertEquals(
                "Engineering",
                profile.getProfessionalInfo().getDepartment()
        );
        assertEquals(
                "Backend Engineer",
                profile.getProfessionalInfo().getJobTitle()
        );

        assertEquals("Building distributed systems", profile.getBio());
        assertEquals("Asia/Kolkata", profile.getTimezone());

        assertEquals(
                ProfileVisibility.ORGANIZATION,
                profile.getVisibility()
        );
        assertEquals(UserStatus.ACTIVE, profile.getStatus());
        assertEquals(Language.ENGLISH, profile.getLanguage());
    }

    @Test
    void updateEntity_shouldUpdateAllEditableFields() {
        UserProfile profile = createProfile();

        UpdateUserProfileRequest request =
                UpdateUserProfileRequest.builder()
                        .firstName("Updated")
                        .lastName("User")
                        .displayName("Updated User")
                        .phoneNumber("9123456780")
                        .organization("Platform Team")
                        .department("Cloud")
                        .jobTitle("Platform Engineer")
                        .bio("Updated biography")
                        .timezone("UTC")
                        .build();

        mapper.updateEntity(profile, request);

        assertEquals("Updated", profile.getPersonalInfo().getFirstName());
        assertEquals("User", profile.getPersonalInfo().getLastName());
        assertEquals(
                "Updated User",
                profile.getPersonalInfo().getDisplayName()
        );

        assertEquals(
                "9123456780",
                profile.getContactInfo().getPhoneNumber()
        );

        assertEquals(
                "Platform Team",
                profile.getProfessionalInfo().getOrganization()
        );
        assertEquals("Cloud", profile.getProfessionalInfo().getDepartment());
        assertEquals(
                "Platform Engineer",
                profile.getProfessionalInfo().getJobTitle()
        );

        assertEquals("Updated biography", profile.getBio());
        assertEquals("UTC", profile.getTimezone());
    }

    @Test
    void updateEntity_shouldInitializeMissingEmbeddedObjects() {
        UserProfile profile = UserProfile.builder()
                .userId(UUID.randomUUID())
                .personalInfo(null)
                .professionalInfo(null)
                .contactInfo(null)
                .build();

        UpdateUserProfileRequest request =
                UpdateUserProfileRequest.builder()
                        .firstName("Test")
                        .lastName("User")
                        .displayName("Test User")
                        .organization("Engineering")
                        .department("Backend")
                        .jobTitle("Developer")
                        .phoneNumber("9876543210")
                        .bio("Test bio")
                        .timezone("UTC")
                        .build();

        assertDoesNotThrow(() -> mapper.updateEntity(profile, request));

        assertNotNull(profile.getPersonalInfo());
        assertNotNull(profile.getProfessionalInfo());
        assertNotNull(profile.getContactInfo());

        assertEquals("Test", profile.getPersonalInfo().getFirstName());
        assertEquals("Engineering", profile.getProfessionalInfo().getOrganization());
        assertEquals("9876543210", profile.getContactInfo().getPhoneNumber());
    }

    @Test
    void toResponse_shouldMapProfileFieldsAndMetadata() {
        UserProfile profile = createProfile();

        profile.setAvatarUrl("https://example.com/avatar.png");
        profile.setVisibility(ProfileVisibility.PUBLIC);
        profile.setStatus(UserStatus.ACTIVE);
        profile.setLanguage(Language.HINDI);
        profile.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        profile.setUpdatedAt(LocalDateTime.of(2026, 2, 1, 12, 0));

        UserProfileResponse response = mapper.toResponse(profile);

        assertNotNull(response);
        assertEquals(profile.getUserId(), response.getUserId());

        assertEquals("Yash", response.getPersonalInfo().getFirstName());
        assertEquals("Sharma", response.getPersonalInfo().getLastName());
        assertEquals("Yash S", response.getPersonalInfo().getDisplayName());

        assertEquals(
                "9876543210",
                response.getContactInfo().getPhoneNumber()
        );

        assertEquals(
                "KubeManager",
                response.getProfessionalInfo().getOrganization()
        );
        assertEquals(
                "Engineering",
                response.getProfessionalInfo().getDepartment()
        );
        assertEquals(
                "Backend Engineer",
                response.getProfessionalInfo().getJobTitle()
        );

        assertEquals("Building distributed systems", response.getBio());
        assertEquals(
                "https://example.com/avatar.png",
                response.getAvatarUrl()
        );
        assertEquals(ProfileVisibility.PUBLIC, response.getVisibility());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertEquals(Language.HINDI, response.getLanguage());
        assertEquals("Asia/Kolkata", response.getTimezone());
        assertEquals(profile.getCreatedAt(), response.getCreatedAt());
        assertEquals(profile.getUpdatedAt(), response.getUpdatedAt());
    }

    private UserProfile createProfile() {
        UserProfile profile = mapper.toEntity(
                CreateUserProfileRequest.builder()
                        .firstName("Yash")
                        .lastName("Sharma")
                        .displayName("Yash S")
                        .phoneNumber("9876543210")
                        .organization("KubeManager")
                        .department("Engineering")
                        .jobTitle("Backend Engineer")
                        .bio("Building distributed systems")
                        .timezone("Asia/Kolkata")
                        .build()
        );

        profile.setId(UUID.randomUUID());
        profile.setUserId(UUID.randomUUID());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        return profile;
    }
}