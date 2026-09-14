package com.cinemahub.cinemahub.security.access;

import com.cinemahub.cinemahub.security.jwt.AuthenticatedUser;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Cierra el IDOR de los endpoints /api/users/{userId}/** que exponen datos propios de
 * un usuario (reservas, notificaciones, pagos): antes, cualquier usuario autenticado
 * podía leer los datos de cualquier otro con solo cambiar el id en la URL.
 *
 * Permite el acceso si el usuario autenticado ES el dueño del recurso, o si tiene
 * ROLE_ADMIN (necesario para la futura pantalla de administración de Pagos, que va
 * a consultar el historial de cualquier usuario).
 *
 * Al lanzar AccessDeniedException (Spring Security), se reutiliza el mismo
 * accessDeniedHandler ya configurado en SecurityConfig — no hace falta tocar esa
 * clase ni el GlobalExceptionHandler.
 */
@Component
public class OwnershipGuard {

    public void requireSelfOrAdmin(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser current)) {
            throw new AccessDeniedException("No tienes permisos para esta acción");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        if (isAdmin || current.userId().equals(userId)) {
            return;
        }

        throw new AccessDeniedException("No tienes permisos para esta acción");
    }
}