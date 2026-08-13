package com.cinemahub.cinemahub.cinema.controller;

import com.cinemahub.cinemahub.cinema.dto.CreateSeatRequest;
import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.cinema.entity.Seat;
import com.cinemahub.cinemahub.cinema.service.SeatService;
import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;

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

@WebMvcTest(SeatController.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private SeatService seatService;

    private Room room() {
        Cinema cinema = new Cinema("Cineplanet San Miguel");
        ReflectionTestUtils.setField(cinema, "id", 1L);
        Room room = new Room(cinema, "Sala 1", 50);
        ReflectionTestUtils.setField(room, "id", 2L);
        return room;
    }

    @Test
    void createReturns201WithValidRequest() throws Exception {
        Seat seat = new Seat(room(), "C", 7);
        ReflectionTestUtils.setField(seat, "id", 100L);
        when(seatService.create(2L, "C", 7)).thenReturn(seat);

        mockMvc.perform(post("/api/rooms/2/seats")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("C", 7))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rowLabel").value("C"))
                .andExpect(jsonPath("$.seatNumber").value(7));
    }

    @Test
    void createReturns400WhenRowLabelIsBlank() throws Exception {
        mockMvc.perform(post("/api/rooms/2/seats")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("", 7))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.rowLabel").exists());
    }

    @Test
    void createReturns409WhenSeatAlreadyExists() throws Exception {
        when(seatService.create(2L, "C", 7))
                .thenThrow(DuplicateResourceException.of("una butaca", "posición", "C7"));

        mockMvc.perform(post("/api/rooms/2/seats")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSeatRequest("C", 7))))
                .andExpect(status().isConflict());
    }
}