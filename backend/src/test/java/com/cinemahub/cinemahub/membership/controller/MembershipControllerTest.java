package com.cinemahub.cinemahub.membership.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.membership.dto.ChangeLevelRequest;
import com.cinemahub.cinemahub.membership.entity.Membership;
import com.cinemahub.cinemahub.membership.service.MembershipService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MembershipController.class)
class MembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private MembershipService membershipService;

    private User user() {
        User user = new User("Sofia", "Test", "sofia@cinemahub.local", "hash");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void createForUserReturns201() throws Exception {
        Membership membership = new Membership(user());
        ReflectionTestUtils.setField(membership, "id", 10L);
        when(membershipService.createForUser(1L)).thenReturn(membership);

        mockMvc.perform(post("/api/users/1/membership"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value("BASIC"))
                .andExpect(jsonPath("$.points").value(0));
    }

    @Test
    void changeLevelReturns400WhenLevelIsNull() throws Exception {
        mockMvc.perform(patch("/api/memberships/10/level")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeLevelRequest(null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.level").exists());
    }

    @Test
    void findByUserReturns404WhenNotFound() throws Exception {
        when(membershipService.findByUser(99L))
                .thenThrow(new ResourceNotFoundException("El usuario 99 no tiene membresía"));

        mockMvc.perform(get("/api/users/99/membership"))
                .andExpect(status().isNotFound());
    }
}