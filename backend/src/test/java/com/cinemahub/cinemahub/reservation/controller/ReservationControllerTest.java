package com.cinemahub.cinemahub.reservation.controller;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.reservation.dto.CreateReservationRequest;
import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.reservation.service.ReservationService;
import com.cinemahub.cinemahub.security.entity.User;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        User user = new User("Ana", "Test", "ana@cinemahub.local", "hash");
        ReflectionTestUtils.setField(user, "id", 1L);
        Reservation reservation = new Reservation(user, OffsetDateTime.now().plusMinutes(10));
        ReflectionTestUtils.setField(reservation, "id", 50L);
        when(reservationService.createReservation(1L, 5L, List.of(10L, 11L))).thenReturn(reservation);

        mockMvc.perform(post("/api/reservations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateReservationRequest(1L, 5L, List.of(10L, 11L)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createReturns400WhenSeatIdsIsEmpty() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateReservationRequest(1L, 5L, List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.seatIds").exists());
    }

    // El caso más importante de todo el proyecto: el intento de doble reserva debe
    // traducirse a 409, no a un 500 ni pasar silenciosamente.
    @Test
    void createReturns409WhenSeatAlreadyReserved() throws Exception {
        when(reservationService.createReservation(anyLong(), anyLong(), anyList()))
                .thenThrow(DuplicateResourceException.of("una reserva", "seatId (función 5)", "10"));

        mockMvc.perform(post("/api/reservations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateReservationRequest(2L, 5L, List.of(10L)))))
                .andExpect(status().isConflict());
    }
}