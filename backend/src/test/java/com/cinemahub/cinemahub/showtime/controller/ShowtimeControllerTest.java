package com.cinemahub.cinemahub.showtime.controller;

import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.entity.Movie;
import com.cinemahub.cinemahub.showtime.dto.CreateShowtimeRequest;
import com.cinemahub.cinemahub.showtime.entity.Showtime;
import com.cinemahub.cinemahub.showtime.service.ShowtimeService;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShowtimeController.class)
class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private ShowtimeService showtimeService;

    private Showtime showtime() {
        Cinema cinema = new Cinema("Cineplanet Jockey Plaza");
        ReflectionTestUtils.setField(cinema, "id", 1L);
        Room room = new Room(cinema, "Sala IMAX", 200);
        ReflectionTestUtils.setField(room, "id", 2L);
        Classification classification = new Classification("PG-13", "Supervisión de adultos");
        Movie movie = new Movie("Interestelar 2", 180, classification);
        ReflectionTestUtils.setField(movie, "id", 3L);
        Showtime showtime = new Showtime(movie, room, LocalDate.now().plusDays(1),
                LocalTime.of(19, 30), LocalTime.of(22, 30), new BigDecimal("35.00"));
        ReflectionTestUtils.setField(showtime, "id", 10L);
        return showtime;
    }

    @Test
    void createReturns201WithValidRequest() throws Exception {
        when(showtimeService.create(any(), any(), any(), any(), any(), any())).thenReturn(showtime());

        CreateShowtimeRequest request = new CreateShowtimeRequest(
                3L, 2L, LocalDate.now().plusDays(1), LocalTime.of(19, 30), LocalTime.of(22, 30),
                new BigDecimal("35.00"));

        mockMvc.perform(post("/api/showtimes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movieTitle").value("Interestelar 2"))
                .andExpect(jsonPath("$.cinemaName").value("Cineplanet Jockey Plaza"));
    }

    @Test
    void createReturns400WhenBasePriceIsNegative() throws Exception {
        CreateShowtimeRequest request = new CreateShowtimeRequest(
                3L, 2L, LocalDate.now().plusDays(1), LocalTime.of(19, 30), LocalTime.of(22, 30),
                new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/showtimes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.basePrice").exists());
    }

    @Test
    void createReturns409WhenShowtimeAlreadyExists() throws Exception {
        when(showtimeService.create(any(), any(), any(), any(), any(), any()))
                .thenThrow(DuplicateResourceException.of("una función", "sala/fecha/hora", "2/2026-09-01/19:30"));

        CreateShowtimeRequest request = new CreateShowtimeRequest(
                3L, 2L, LocalDate.now().plusDays(1), LocalTime.of(19, 30), LocalTime.of(22, 30),
                new BigDecimal("35.00"));

        mockMvc.perform(post("/api/showtimes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}