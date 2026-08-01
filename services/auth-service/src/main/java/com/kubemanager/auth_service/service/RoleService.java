package com.kubemanager.auth_service.service;

import com.kubemanager.auth_service.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    Role save(Role role);

    Optional<Role> findByName(String name);

    List<Role> findAll();

}
