package com.kubemanager.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.user_service.dtos.request.CreateUserProfileRequest;
import com.kubemanager.user_service.dtos.request.UpdateUserProfileRequest;
import com.kubemanager.user_service.dtos.response.UserProfileResponse;
import com.kubemanager.user_service.enums.Language;
import com.kubemanager.user_service.enums.ProfileVisibility;
import com.kubemanager.user_service.enums.UserStatus;
import com.kubemanager.user_service.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserProfileService userProfileService;

    private UUID userId;
    private UserProfileResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        response = UserProfileResponse.builder()
                .userId(userId)
                .bio("Backend engineer")
                .avatarUrl("https://example.com/avatar.png")
                .visibility(ProfileVisibility.ORGANIZATION)
                .status(UserStatus.ACTIVE)
                .language(Language.ENGLISH)
                .timezone("Asia/Kolkata")
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
    }

    @Test
    void createProfile_shouldReturnCreated() throws Exception {
        CreateUserProfileRequest request =
                CreateUserProfileRequest.builder()
                        .firstName("Test")
                        .lastName("User")
                        .displayName("Test User")
                        .build();

        when(userProfileService.createProfile(any(CreateUserProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(userProfileService).createProfile(any(CreateUserProfileRequest.class));
    }

    @Test
    void getCurrentUserProfile_shouldReturnOk() throws Exception {
        when(userProfileService.getCurrentUserProfile()).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk());

        verify(userProfileService).getCurrentUserProfile();
    }

    @Test
    void getProfileByUserId_shouldReturnOk() throws Exception {
        when(userProfileService.getProfileByUserId(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userProfileService).getProfileByUserId(userId);
    }

    @Test
    void getProfileByUserId_shouldRejectInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        verify(userProfileService, never()).getProfileByUserId(any());
    }

    @Test
    void updateProfile_shouldReturnOk() throws Exception {
        UpdateUserProfileRequest request =
                UpdateUserProfileRequest.builder()
                        .firstName("Updated")
                        .displayName("Updated User")
                        .build();

        when(userProfileService.updateProfile(any(UpdateUserProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userProfileService).updateProfile(any(UpdateUserProfileRequest.class));
    }

    @Test
    void deleteProfile_shouldReturnOk() throws Exception {
        doNothing().when(userProfileService).deleteProfile();

        mockMvc.perform(delete("/api/v1/users/profile"))
                .andExpect(status().isOk());

        verify(userProfileService).deleteProfile();
    }
}