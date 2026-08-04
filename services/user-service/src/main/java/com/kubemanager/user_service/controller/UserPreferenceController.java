package com.kubemanager.user_service.controller;


import com.kubemanager.response.ApiResponse;
import com.kubemanager.user_service.dtos.request.UpdateUserPreferenceRequest;
import com.kubemanager.user_service.dtos.response.UserPreferenceResponse;
import com.kubemanager.user_service.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/preferences")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public ApiResponse<UserPreferenceResponse> getPreferences() {

        return ApiResponse.success(
                "User preferences fetched successfully.",
                userPreferenceService.getUserPreference()
        );
    }

    @PutMapping
    public ApiResponse<UserPreferenceResponse> updatePreferences(
            @Valid @RequestBody UpdateUserPreferenceRequest request
    ) {

        return ApiResponse.success(
                "User preferences updated successfully.",
                userPreferenceService.updatePreferences(request)
        );
    }

    @PostMapping("/reset")
    public ApiResponse<UserPreferenceResponse> resetPreferences() {

        return ApiResponse.success(
                "User preferences reset successfully.",
                userPreferenceService.resetPreferences()
        );
    }
}