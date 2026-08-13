package com.cinemahub.cinemahub.notification.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.notification.dto.CreateNotificationRequest;
import com.cinemahub.cinemahub.notification.dto.NotificationResponse;
import com.cinemahub.cinemahub.notification.entity.Notification;
import com.cinemahub.cinemahub.notification.service.NotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/api/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
        Notification notification = notificationService.create(
                request.userId(), request.title(), request.message());
        return NotificationResponse.from(notification);
    }

    @GetMapping("/api/users/{userId}/notifications")
    public List<NotificationResponse> findByUser(@PathVariable Long userId) {
        return notificationService.findByUser(userId).stream().map(NotificationResponse::from).toList();
    }

    @GetMapping("/api/users/{userId}/notifications/unread")
    public List<NotificationResponse> findUnread(@PathVariable Long userId) {
        return notificationService.findUnread(userId).stream().map(NotificationResponse::from).toList();
    }

    @PatchMapping("/api/notifications/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id) {
        return NotificationResponse.from(notificationService.markAsRead(id));
    }

    @PatchMapping("/api/users/{userId}/notifications/read-all")
    public Map<String, Integer> markAllAsRead(@PathVariable Long userId) {
        int updated = notificationService.markAllAsRead(userId);
        return Map.of("updated", updated);
    }
}