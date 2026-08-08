package com.cinemahub.cinemahub.security.dto;

import com.cinemahub.cinemahub.security.entity.Permission;

public record PermissionResponse(Long id, String name, String description) {

    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getName(), permission.getDescription());
    }
}