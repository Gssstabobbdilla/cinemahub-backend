package com.cinemahub.cinemahub.notification.dto;

import com.cinemahub.cinemahub.notification.entity.Notification;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        boolean read,
        OffsetDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(), notification.getUser().getId(), notification.getTitle(),
                notification.getMessage(), notification.isRead(), notification.getCreatedAt());
    }
}