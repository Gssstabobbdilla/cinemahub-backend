package com.cinemahub.cinemahub.security.controller;

import com.cinemahub.cinemahub.security.dto.AssignPermissionRequest;
import com.cinemahub.cinemahub.security.dto.PermissionResponse;
import com.cinemahub.cinemahub.security.dto.RoleRequest;
import com.cinemahub.cinemahub.security.dto.RoleResponse;
import com.cinemahub.cinemahub.security.entity.Role;
import com.cinemahub.cinemahub.security.service.RoleService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponse> findAll() {
        return roleService.findAll().stream().map(RoleResponse::from).toList();
    }

    @GetMapping("/{id}")
    public RoleResponse findById(@PathVariable Long id) {
        return RoleResponse.from(roleService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@RequestBody RoleRequest request) {
        Role role = roleService.create(request.name(), request.description());
        return RoleResponse.from(role);
    }

    @PutMapping("/{id}")
    public RoleResponse update(@PathVariable Long id, @RequestBody RoleRequest request) {
        Role role = roleService.update(id, request.name(), request.description());
        return RoleResponse.from(role);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }

    @GetMapping("/{id}/permissions")
    public List<PermissionResponse> findPermissions(@PathVariable Long id) {
        return roleService.findPermissions(id).stream()
                .map(rolePermission -> PermissionResponse.from(rolePermission.getPermission()))
                .toList();
    }

    @PostMapping("/{id}/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    public void assignPermission(@PathVariable Long id, @RequestBody AssignPermissionRequest request) {
        roleService.assignPermission(id, request.permissionId());
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePermission(@PathVariable Long id, @PathVariable Long permissionId) {
        roleService.removePermission(id, permissionId);
    }
}