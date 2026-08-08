package com.cinemahub.cinemahub.notification.dto;

public record CreateNotificationRequest(Long userId, String title, String message) {
}