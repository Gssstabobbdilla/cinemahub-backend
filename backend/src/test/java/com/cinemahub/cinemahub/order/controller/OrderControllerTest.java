package com.cinemahub.cinemahub.order.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.order.dto.AddProductRequest;
import com.cinemahub.cinemahub.order.entity.Order;
import com.cinemahub.cinemahub.order.service.OrderService;
import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.security.entity.User;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createFromReservationReturns201() throws Exception {
        User user = new User("Pepe", "Test", "pepe@cinemahub.local", "hash");
        Reservation reservation = new Reservation(user, OffsetDateTime.now().plusMinutes(10));
        ReflectionTestUtils.setField(reservation, "id", 7L);
        Order order = new Order(reservation, new BigDecimal("50.00"));
        ReflectionTestUtils.setField(order, "id", 1L);
        when(orderService.createFromReservation(7L)).thenReturn(order);

        mockMvc.perform(post("/api/reservations/7/order"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(50.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void addProductReturns400WhenQuantityIsNegative() throws Exception {
        mockMvc.perform(post("/api/orders/1/products")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddProductRequest(3L, -2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.quantity").exists());
    }

    @Test
    void createFromReservationReturns404WhenReservationNotFound() throws Exception {
        when(orderService.createFromReservation(99L))
                .thenThrow(new ResourceNotFoundException("Reservation no encontrado: id=99"));

        mockMvc.perform(post("/api/reservations/99/order"))
                .andExpect(status().isNotFound());
    }
}