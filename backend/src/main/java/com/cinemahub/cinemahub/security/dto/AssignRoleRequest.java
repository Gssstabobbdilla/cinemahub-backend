package com.cinemahub.cinemahub.security.dto;

import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(@NotNull Long roleId) {
}