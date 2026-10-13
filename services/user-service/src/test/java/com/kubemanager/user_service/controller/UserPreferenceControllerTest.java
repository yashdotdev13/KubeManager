package com.kubemanager.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.user_service.dtos.request.UpdateUserPreferenceRequest;
import com.kubemanager.user_service.dtos.response.UserPreferenceResponse;
import com.kubemanager.user_service.enums.Theme;
import com.kubemanager.user_service.service.UserPreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserPreferenceController.class)
class UserPreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserPreferenceService userPreferenceService;

    @Test
    void getUserPreference_shouldReturnOk() throws Exception {
        when(userPreferenceService.getUserPreference())
                .thenReturn(UserPreferenceResponse.builder().build());

        mockMvc.perform(get("/api/v1/users/preferences"))
                .andExpect(status().isOk());

        verify(userPreferenceService).getUserPreference();
    }

    @Test
    void updatePreferences_shouldReturnOk() throws Exception {
        UpdateUserPreferenceRequest request =
                UpdateUserPreferenceRequest.builder()
                        .theme(Theme.DARK)
                        .defaultCluster("production")
                        .defaultNamespace("monitoring")
                        .emailNotification(false)
                        .slackNotification(true)
                        .desktopNotification(false)
                        .aiSuggestionsEnabled(true)
                        .allowAiClusterActions(false)
                        .build();

        when(userPreferenceService.updatePreferences(
                any(UpdateUserPreferenceRequest.class)))
                .thenReturn(UserPreferenceResponse.builder().build());

        mockMvc.perform(put("/api/v1/users/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userPreferenceService)
                .updatePreferences(any(UpdateUserPreferenceRequest.class));
    }

    @Test
    void resetPreferences_shouldReturnOk() throws Exception {
        when(userPreferenceService.resetPreferences())
                .thenReturn(UserPreferenceResponse.builder().build());

        mockMvc.perform(post("/api/v1/users/preferences/reset"))
                .andExpect(status().isOk());

        verify(userPreferenceService).resetPreferences();
    }
}