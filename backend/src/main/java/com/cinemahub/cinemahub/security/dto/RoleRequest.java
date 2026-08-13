package com.cinemahub.cinemahub.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Se usa tanto para crear como para actualizar (RoleService.create/update tienen la misma forma).
public record RoleRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 255) String description
) {
}