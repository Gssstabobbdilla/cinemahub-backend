package com.cinemahub.cinemahub.security.controller;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.security.dto.AuthResponse;
import com.cinemahub.cinemahub.security.dto.LoginRequest;
import com.cinemahub.cinemahub.security.dto.RegisterUserRequest;
import com.cinemahub.cinemahub.security.entity.User;
import com.cinemahub.cinemahub.security.service.AuthService;
import com.cinemahub.cinemahub.security.service.UserService;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;


import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cinemahub.cinemahub.security.jwt.JwtService;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;
    
    @MockitoBean
    private JwtService jwtService;

    @Test
    void loginReturns200WithTokenOnValidCredentials() throws Exception {
        AuthResponse response = new AuthResponse(
                "fake.jwt.token", 1L, "Ana", "Test", "ana@cinemahub.local", List.of("ROLE_USER"));
        when(authService.login("ana@cinemahub.local", "clave12345")).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("ana@cinemahub.local", "clave12345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.email").value("ana@cinemahub.local"));
    }

    @Test
    void loginReturns401WhenCredentialsAreInvalid() throws Exception {
        when(authService.login("ana@cinemahub.local", "clave-mala"))
                .thenThrow(new BadCredentialsException("Email o contraseña incorrectos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("ana@cinemahub.local", "clave-mala"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturns400WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("no-es-un-email", "clave12345"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void registerReturns201WithTokenAndNeverExposesPassword() throws Exception {
        User user = new User("Ana", "Test", "ana@cinemahub.local", "hashed");
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userService.register("Ana", "Test", "ana@cinemahub.local", "clave12345")).thenReturn(user);

        AuthResponse response = new AuthResponse(
                "fake.jwt.token", 1L, "Ana", "Test", "ana@cinemahub.local", List.of("ROLE_USER"));
        when(authService.buildResponse(user)).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterUserRequest("Ana", "Test", "ana@cinemahub.local", "clave12345"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registerReturns409WhenEmailAlreadyExists() throws Exception {
        when(userService.register("Ana", "Test", "ana@cinemahub.local", "clave12345"))
                .thenThrow(DuplicateResourceException.of("un usuario", "email", "ana@cinemahub.local"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterUserRequest("Ana", "Test", "ana@cinemahub.local", "clave12345"))))
                .andExpect(status().isConflict());
    }
}