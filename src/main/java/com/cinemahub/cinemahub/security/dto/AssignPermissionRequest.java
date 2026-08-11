package com.cinemahub.cinemahub.security.dto;

import jakarta.validation.constraints.NotNull;

public record AssignPermissionRequest(@NotNull Long permissionId) {
}