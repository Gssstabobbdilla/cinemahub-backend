package com.cinemahub.cinemahub.security.jwt;

// Principal que JwtAuthFilter deja en el SecurityContext. Lleva el userId además del
// email para que los controllers puedan verificar "¿sos dueño de este recurso?" sin
// una consulta extra a la BD — ver OwnershipGuard.
public record AuthenticatedUser(Long userId, String email) {
}