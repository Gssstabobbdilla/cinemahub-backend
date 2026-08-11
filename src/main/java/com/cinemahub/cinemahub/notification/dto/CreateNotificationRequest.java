package com.cinemahub.cinemahub.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 150) String title,
        String message
) {
}