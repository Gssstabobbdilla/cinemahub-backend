package com.cinemahub.cinemahub.cinema.controller;

import com.cinemahub.cinemahub.cinema.dto.CreateCinemaRequest;
import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.service.CinemaService;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CinemaController.class)
class CinemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private CinemaService cinemaService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        Cinema cinema = new Cinema("Cineplanet Alcázar");
        ReflectionTestUtils.setField(cinema, "id", 1L);
        when(cinemaService.create("Cineplanet Alcázar")).thenReturn(cinema);

        mockMvc.perform(post("/api/cinemas")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCinemaRequest("Cineplanet Alcázar"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cineplanet Alcázar"));
    }

    @Test
    void createReturns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/cinemas")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCinemaRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(cinemaService.findById(99L)).thenThrow(new ResourceNotFoundException("Cinema no encontrado: id=99"));

        mockMvc.perform(get("/api/cinemas/99"))
                .andExpect(status().isNotFound());
    }
}