package com.kubemanager.user_service.controller;


import com.kubemanager.response.ApiResponse;
import com.kubemanager.user_service.dtos.request.CreateUserProfileRequest;
import com.kubemanager.user_service.dtos.request.UpdateUserProfileRequest;
import com.kubemanager.user_service.dtos.response.UserProfileResponse;
import com.kubemanager.user_service.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/profile")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserProfileResponse> createProfile(
            @Valid @RequestBody CreateUserProfileRequest request
    ) {

        return ApiResponse.success(
                "Profile created successfully.",
                userProfileService.createProfile(request)
        );
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentProfile() {

        return ApiResponse.success(
                "Profile fetched successfully.",
                userProfileService.getCurrentUserProfile()
        );
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getProfileByUserId(
            @PathVariable UUID userId
    ) {

        return ApiResponse.success(
                "Profile fetched successfully.",
                userProfileService.getProfileByUserId(userId)
        );
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {

        return ApiResponse.success(
                "Profile updated successfully.",
                userProfileService.updateProfile(request)
        );
    }

    @DeleteMapping("/profile")
    public ApiResponse<Void> deleteProfile() {

        userProfileService.deleteProfile();

        return ApiResponse.success(
                "Profile deleted successfully.",
                null
        );
    }
}