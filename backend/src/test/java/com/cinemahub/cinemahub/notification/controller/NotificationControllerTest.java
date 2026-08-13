package com.cinemahub.cinemahub.notification.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.notification.dto.CreateNotificationRequest;
import com.cinemahub.cinemahub.notification.entity.Notification;
import com.cinemahub.cinemahub.notification.service.NotificationService;
import com.cinemahub.cinemahub.security.entity.User;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        User user = new User("Vale", "Test", "vale@cinemahub.local", "hash");
        ReflectionTestUtils.setField(user, "id", 1L);
        Notification notification = new Notification(user, "Tu función empieza pronto");
        ReflectionTestUtils.setField(notification, "id", 3L);
        when(notificationService.create(1L, "Tu función empieza pronto", "Llega 15 min antes"))
                .thenReturn(notification);

        mockMvc.perform(post("/api/notifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateNotificationRequest(1L, "Tu función empieza pronto", "Llega 15 min antes"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Tu función empieza pronto"))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void createReturns400WhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateNotificationRequest(1L, "", "mensaje"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void createReturns404WhenUserNotFound() throws Exception {
        when(notificationService.create(99L, "Título", "mensaje"))
                .thenThrow(new ResourceNotFoundException("User no encontrado: id=99"));

        mockMvc.perform(post("/api/notifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateNotificationRequest(99L, "Título", "mensaje"))))
                .andExpect(status().isNotFound());
    }
}