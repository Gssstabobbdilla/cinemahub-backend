package com.cinemahub.cinemahub.movie.controller;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.movie.dto.GenreRequest;
import com.cinemahub.cinemahub.movie.entity.Genre;
import com.cinemahub.cinemahub.movie.service.GenreService;

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

@WebMvcTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private GenreService genreService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        Genre genre = new Genre("Ciencia ficción");
        ReflectionTestUtils.setField(genre, "id", 1L);
        when(genreService.create("Ciencia ficción")).thenReturn(genre);

        mockMvc.perform(post("/api/genres")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenreRequest("Ciencia ficción"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ciencia ficción"));
    }

    @Test
    void createReturns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/genres")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenreRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void createReturns409WhenNameAlreadyExists() throws Exception {
        when(genreService.create("Drama"))
                .thenThrow(DuplicateResourceException.of("un género", "name", "Drama"));

        mockMvc.perform(post("/api/genres")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenreRequest("Drama"))))
                .andExpect(status().isConflict());
    }
}