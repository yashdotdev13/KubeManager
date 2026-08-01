package com.kubemanager.auth_service.config;

import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleService roleService;

    @Override
    public void run(String... args) {

        createRoleIfNotExists("ROLE_USER");
        createRoleIfNotExists("ROLE_ADMIN");

    }

    private void createRoleIfNotExists(String roleName) {

        roleService.findByName(roleName)
                .orElseGet(() ->
                        roleService.save(
                                Role.builder()
                                        .name(roleName)
                                        .build()
                        )
                );
    }
}