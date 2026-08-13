package com.cinemahub.cinemahub.security.dto;

import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.entity.UserStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

// Nunca incluye password: el hash (o, por ahora, el placeholder en texto plano — ver
// TODO en UserService.register) no debe salir en ninguna respuesta de la API.
public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate birthDate,
        UserStatus status,
        OffsetDateTime createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPhone(), user.getBirthDate(), user.getStatus(), user.getCreatedAt());
    }
}