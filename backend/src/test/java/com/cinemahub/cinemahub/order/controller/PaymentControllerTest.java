package com.cinemahub.cinemahub.order.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.order.entity.Order;
import com.cinemahub.cinemahub.order.entity.Payment;
import com.cinemahub.cinemahub.order.entity.PaymentStatus;
import com.cinemahub.cinemahub.order.service.PaymentService;
import com.cinemahub.cinemahub.reservation.entity.Reservation;
import com.cinemahub.cinemahub.security.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// PaymentController no recibe ningún @RequestBody (todos sus endpoints son GET o POST sin
// body), así que no hay caso de "validación rechaza" que probar aquí — se reemplaza por un
// tercer caso (refund) que sigue siendo comportamiento real del controller.
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    private Payment payment(PaymentStatus status) {
        User user = new User("Nico", "Test", "nico@cinemahub.local", "hash");
        Reservation reservation = new Reservation(user, OffsetDateTime.now().plusMinutes(10));
        Order order = new Order(reservation, new BigDecimal("20.00"));
        ReflectionTestUtils.setField(order, "id", 1L);
        Payment payment = new Payment(order, "CARD", new BigDecimal("20.00"));
        ReflectionTestUtils.setField(payment, "id", 3L);
        ReflectionTestUtils.setField(payment, "status", status);
        return payment;
    }

    @Test
    void findByIdReturns200() throws Exception {
        when(paymentService.findById(3L)).thenReturn(payment(PaymentStatus.APPROVED));

        mockMvc.perform(get("/api/payments/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(paymentService.findById(99L)).thenThrow(new ResourceNotFoundException("Payment no encontrado: id=99"));

        mockMvc.perform(get("/api/payments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void refundReturns200WithRefundedStatus() throws Exception {
        when(paymentService.refund(3L)).thenReturn(payment(PaymentStatus.REFUNDED));

        mockMvc.perform(post("/api/payments/3/refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}