package com.kubemanager.auth_service.mapper;


import com.kubemanager.auth_service.dto.response.UserResponse;
import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet())
                )
                .build();
    }

}