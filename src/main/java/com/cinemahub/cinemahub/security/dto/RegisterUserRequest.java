package com.cinemahub.cinemahub.security.dto;

// El password llega en texto plano desde el cliente (vía HTTPS). UserService.register
// todavía no lo encripta (ver TODO ahí); cuando se agregue Spring Security, el hashing
// se hace antes de llamar al service, no acá.
public record RegisterUserRequest(String firstName, String lastName, String email, String password) {
}