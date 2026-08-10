package com.cinemahub.cinemahub.security.controller;

import com.cinemahub.cinemahub.security.dto.PermissionRequest;
import com.cinemahub.cinemahub.security.dto.PermissionResponse;
import com.cinemahub.cinemahub.security.entity.Permission;
import com.cinemahub.cinemahub.security.service.PermissionService;

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
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public List<PermissionResponse> findAll() {
        return permissionService.findAll().stream().map(PermissionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PermissionResponse findById(@PathVariable Long id) {
        return PermissionResponse.from(permissionService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse create(@RequestBody PermissionRequest request) {
        Permission permission = permissionService.create(request.name(), request.description());
        return PermissionResponse.from(permission);
    }

    @PutMapping("/{id}")
    public PermissionResponse update(@PathVariable Long id, @RequestBody PermissionRequest request) {
        Permission permission = permissionService.update(id, request.name(), request.description());
        return PermissionResponse.from(permission);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        permissionService.delete(id);
    }
}