package com.cinemahub.cinemahub.security.controller;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.security.dto.RegisterUserRequest;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.service.UserService;

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

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void registerReturns201WithValidRequestAndNeverExposesPassword() throws Exception {
        User user = new User("Ana", "Test", "ana@cinemahub.local", "hashed");
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userService.register("Ana", "Test", "ana@cinemahub.local", "clave12345")).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterUserRequest("Ana", "Test", "ana@cinemahub.local", "clave12345"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@cinemahub.local"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerReturns400WithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterUserRequest("Ana", "Test", "no-es-un-email", "clave12345"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void registerReturns409WhenEmailAlreadyExists() throws Exception {
        when(userService.register("Ana", "Test", "ana@cinemahub.local", "clave12345"))
                .thenThrow(DuplicateResourceException.of("un usuario", "email", "ana@cinemahub.local"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterUserRequest("Ana", "Test", "ana@cinemahub.local", "clave12345"))))
                .andExpect(status().isConflict());
    }
}