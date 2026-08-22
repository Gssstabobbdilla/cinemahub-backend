package com.cinemahub.cinemahub.movie.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.movie.dto.CreateMovieRequest;
import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.movie.service.MovieService;
import com.cinemahub.cinemahub.movie.dto.UpdateMovieRequest;

import tools.jackson.databind.json.JsonMapper;
import java.time.LocalDate;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private MovieService movieService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        Classification classification = new Classification("PG-13", "Supervisión de adultos");
        ReflectionTestUtils.setField(classification, "id", 1L);
        Movie movie = new Movie("Dune: Parte 3", 165, classification);
        ReflectionTestUtils.setField(movie, "id", 10L);
        when(movieService.create("Dune: Parte 3", 165, 1L)).thenReturn(movie);

        mockMvc.perform(post("/api/movies")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMovieRequest("Dune: Parte 3", 165, 1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Dune: Parte 3"))
                .andExpect(jsonPath("$.classification.code").value("PG-13"));
    }

    @Test
    void createReturns400WhenDurationIsNegative() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateMovieRequest("Película Test", -5, 1L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.duration").exists());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(movieService.findById(99L)).thenThrow(new ResourceNotFoundException("Movie no encontrado: id=99"));

        mockMvc.perform(get("/api/movies/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReturns200WithValidRequest() throws Exception {
        Classification classification = new Classification("PG-13", "Supervisión de adultos");
        ReflectionTestUtils.setField(classification, "id", 1L);
        Movie movie = new Movie("Dune: Parte 3 (Extendida)", 180, classification);
        ReflectionTestUtils.setField(movie, "id", 10L);
        when(movieService.update(10L, "Dune: Parte 3 (Extendida)", "Nueva sinopsis", 180,
                LocalDate.of(2026, 12, 1), "https://x.com/poster.png", "https://x.com/trailer.mp4", 1L))
                .thenReturn(movie);

        mockMvc.perform(put("/api/movies/10")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateMovieRequest("Dune: Parte 3 (Extendida)", "Nueva sinopsis", 180,
                                        LocalDate.of(2026, 12, 1), "https://x.com/poster.png",
                                        "https://x.com/trailer.mp4", 1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune: Parte 3 (Extendida)"));
    }

    @Test
    void updateReturns400WhenTitleIsBlank() throws Exception {
        mockMvc.perform(put("/api/movies/10")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateMovieRequest("", null, 100, null, null, null, 1L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.title").exists());
    }

    @Test
    void updateReturns404WhenMovieNotFound() throws Exception {
        when(movieService.update(99L, "X", null, 100, null, null, null, 1L))
                .thenThrow(new ResourceNotFoundException("Movie no encontrado: id=99"));

        mockMvc.perform(put("/api/movies/99")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateMovieRequest("X", null, 100, null, null, null, 1L))))
                .andExpect(status().isNotFound());
    }
}