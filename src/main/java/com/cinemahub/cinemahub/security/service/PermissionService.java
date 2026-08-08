package com.cinemahub.cinemahub.security.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.security.entity.Permission;
import com.cinemahub.cinemahub.security.repository.PermissionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Permission findById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Permission", id));
    }

    @Transactional
    public Permission create(String name, String description) {
        permissionRepository.findByName(name).ifPresent(existing -> {
            throw DuplicateResourceException.of("un permiso", "name", name);
        });
        return permissionRepository.save(new Permission(name, description));
    }

    @Transactional
    public Permission update(Long id, String name, String description) {
        Permission permission = findById(id);
        if (!permission.getName().equals(name)) {
            permissionRepository.findByName(name).ifPresent(existing -> {
                throw DuplicateResourceException.of("un permiso", "name", name);
            });
        }
        permission.setName(name);
        permission.setDescription(description);
        return permission;
    }

    @Transactional
    public void delete(Long id) {
        permissionRepository.delete(findById(id));
    }
}