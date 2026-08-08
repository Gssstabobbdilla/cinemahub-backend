package com.cinemahub.cinemahub.notification.repository;

import com.cinemahub.cinemahub.notification.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndRead(Long userId, boolean read);
}