package com.cinemahub.cinemahub.security.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.security.dto.RoleRequest;
import com.cinemahub.cinemahub.security.entity.Role;
import com.cinemahub.cinemahub.security.service.RoleService;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        Role role = new Role("ADMIN", "Administrador");
        ReflectionTestUtils.setField(role, "id", 1L);
        when(roleService.create("ADMIN", "Administrador")).thenReturn(role);

        mockMvc.perform(post("/api/roles")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest("ADMIN", "Administrador"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    void createReturns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/roles")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RoleRequest("", "desc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(roleService.findById(99L)).thenThrow(new ResourceNotFoundException("Role no encontrado: id=99"));

        mockMvc.perform(get("/api/roles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role no encontrado: id=99"));
    }
}