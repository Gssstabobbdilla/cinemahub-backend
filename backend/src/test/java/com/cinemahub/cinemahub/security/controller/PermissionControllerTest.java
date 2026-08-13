package com.cinemahub.cinemahub.security.controller;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.security.dto.PermissionRequest;
import com.cinemahub.cinemahub.security.entity.Permission;
import com.cinemahub.cinemahub.security.service.PermissionService;

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

@WebMvcTest(PermissionController.class)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        Permission permission = new Permission("MOVIE_WRITE", "Crear y editar películas");
        ReflectionTestUtils.setField(permission, "id", 1L);
        when(permissionService.create("MOVIE_WRITE", "Crear y editar películas")).thenReturn(permission);

        mockMvc.perform(post("/api/permissions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PermissionRequest("MOVIE_WRITE", "Crear y editar películas"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("MOVIE_WRITE"));
    }

    @Test
    void createReturns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/permissions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PermissionRequest("", "desc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void createReturns409WhenNameAlreadyExists() throws Exception {
        when(permissionService.create("MOVIE_WRITE", "desc"))
                .thenThrow(DuplicateResourceException.of("un permiso", "name", "MOVIE_WRITE"));

        mockMvc.perform(post("/api/permissions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PermissionRequest("MOVIE_WRITE", "desc"))))
                .andExpect(status().isConflict());
    }
}