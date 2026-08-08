package com.cinemahub.cinemahub.notification.service;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.notification.entity.Notification;
import com.cinemahub.cinemahub.notification.repository.NotificationRepository;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
    }

    public List<Notification> findByUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> findUnread(Long userId) {
        return notificationRepository.findByUserIdAndRead(userId, false);
    }

    @Transactional
    public Notification create(Long userId, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        Notification notification = new Notification(user, title);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsRead(Long id) {
        Notification notification = findById(id);
        notification.setRead(true);
        return notification;
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        List<Notification> unread = findUnread(userId);
        unread.forEach(n -> n.setRead(true));
        return unread.size();
    }
}