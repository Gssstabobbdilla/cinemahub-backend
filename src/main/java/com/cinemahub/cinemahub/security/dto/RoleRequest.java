package com.cinemahub.cinemahub.security.dto;

// Se usa tanto para crear como para actualizar (RoleService.create/update tienen la misma forma).
public record RoleRequest(String name, String description) {
}