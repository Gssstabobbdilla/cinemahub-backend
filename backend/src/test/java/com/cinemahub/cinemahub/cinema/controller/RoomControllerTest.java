package com.cinemahub.cinemahub.cinema.controller;

import com.cinemahub.cinemahub.cinema.dto.CreateRoomRequest;
import com.cinemahub.cinemahub.cinema.entity.Cinema;
import com.cinemahub.cinemahub.cinema.entity.Room;
import com.cinemahub.cinemahub.cinema.service.RoomService;
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

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private RoomService roomService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        Cinema cinema = new Cinema("Cineplanet Real Plaza");
        ReflectionTestUtils.setField(cinema, "id", 1L);
        Room room = new Room(cinema, "Sala 3", 80);
        ReflectionTestUtils.setField(room, "id", 5L);
        when(roomService.create(1L, "Sala 3", 80)).thenReturn(room);

        mockMvc.perform(post("/api/cinemas/1/rooms")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRoomRequest("Sala 3", 80))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sala 3"))
                .andExpect(jsonPath("$.capacity").value(80));
    }

    @Test
    void createReturns400WhenCapacityIsNegative() throws Exception {
        mockMvc.perform(post("/api/cinemas/1/rooms")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateRoomRequest("Sala 3", -10))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.capacity").exists());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(roomService.findById(99L)).thenThrow(new ResourceNotFoundException("Room no encontrado: id=99"));

        mockMvc.perform(get("/api/rooms/99"))
                .andExpect(status().isNotFound());
    }
}