package com.cinemahub.cinemahub.security.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.security.entity.Permission;
import com.cinemahub.cinemahub.security.entity.Role;
import com.cinemahub.cinemahub.security.entity.RolePermission;
import com.cinemahub.cinemahub.security.entity.RolePermissionId;
import com.cinemahub.cinemahub.security.repository.PermissionRepository;
import com.cinemahub.cinemahub.security.repository.RolePermissionRepository;
import com.cinemahub.cinemahub.security.repository.RoleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RoleService(RoleRepository roleRepository,
                        PermissionRepository permissionRepository,
                        RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", id));
    }

    @Transactional
    public Role create(String name, String description) {
        roleRepository.findByName(name).ifPresent(existing -> {
            throw DuplicateResourceException.of("un rol", "name", name);
        });
        Role role = new Role(name, description);
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Long id, String name, String description) {
        Role role = findById(id);
        if (!role.getName().equals(name)) {
            roleRepository.findByName(name).ifPresent(existing -> {
                throw DuplicateResourceException.of("un rol", "name", name);
            });
        }
        role.setName(name);
        role.setDescription(description);
        return role;
    }

    @Transactional
    public void delete(Long id) {
        Role role = findById(id);
        roleRepository.delete(role);
    }

    @Transactional
    public RolePermission assignPermission(Long roleId, Long permissionId) {
        RolePermissionId id = new RolePermissionId(roleId, permissionId);
        return rolePermissionRepository.findById(id).orElseGet(() -> {
            Role role = findById(roleId);
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> ResourceNotFoundException.of("Permission", permissionId));
            return rolePermissionRepository.save(new RolePermission(role, permission));
        });
    }

    @Transactional
    public void removePermission(Long roleId, Long permissionId) {
        rolePermissionRepository.deleteById(new RolePermissionId(roleId, permissionId));
    }

    public List<RolePermission> findPermissions(Long roleId) {
        return rolePermissionRepository.findById_RoleId(roleId);
    }
}