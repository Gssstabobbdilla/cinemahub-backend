package com.cinemahub.cinemahub.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// El password llega en texto plano desde el cliente (vía HTTPS). UserService.register
// todavía no lo encripta (ver TODO ahí); cuando se agregue Spring Security, el hashing
// se hace antes de llamar al service, no acá.
public record RegisterUserRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 255) String password
) {
}