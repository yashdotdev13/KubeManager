package com.kubemanager.auth_service.service;


import com.kubemanager.auth_service.entity.Role;
import com.kubemanager.auth_service.repository.RoleRepository;

import com.kubemanager.auth_service.service.Impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();

        adminRole = Role.builder()
                .id(2L)
                .name("ROLE_ADMIN")
                .build();
    }

    @Test
    void save_shouldReturnSavedRole() {
        when(roleRepository.save(userRole)).thenReturn(userRole);

        Role result = roleService.save(userRole);

        assertSame(userRole, result);
        verify(roleRepository).save(userRole);
    }

    @Test
    void findByName_shouldReturnRoleWhenFound() {
        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        Optional<Role> result = roleService.findByName("ROLE_USER");

        assertTrue(result.isPresent());
        assertSame(userRole, result.get());
        verify(roleRepository).findByName("ROLE_USER");
    }

    @Test
    void findByName_shouldReturnEmptyWhenRoleDoesNotExist() {
        when(roleRepository.findByName("ROLE_UNKNOWN"))
                .thenReturn(Optional.empty());

        Optional<Role> result = roleService.findByName("ROLE_UNKNOWN");

        assertTrue(result.isEmpty());
        verify(roleRepository).findByName("ROLE_UNKNOWN");
    }

    @Test
    void findAll_shouldReturnAllRoles() {
        when(roleRepository.findAll())
                .thenReturn(List.of(userRole, adminRole));

        List<Role> result = roleService.findAll();

        assertEquals(2, result.size());
        assertEquals("ROLE_USER", result.get(0).getName());
        assertEquals("ROLE_ADMIN", result.get(1).getName());
        verify(roleRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoRolesExist() {
        when(roleRepository.findAll()).thenReturn(List.of());

        List<Role> result = roleService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roleRepository).findAll();
    }
}