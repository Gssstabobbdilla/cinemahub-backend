package com.cinemahub.cinemahub.promotion.controller;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.promotion.dto.GenerateCouponRequest;
import com.cinemahub.cinemahub.promotion.entity.Coupon;
import com.cinemahub.cinemahub.promotion.entity.Promotion;
import com.cinemahub.cinemahub.promotion.service.CouponService;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    private Promotion promotion() {
        Promotion promotion = new Promotion(
                "Descuento estudiantes", new BigDecimal("20.00"), LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(promotion, "id", 1L);
        return promotion;
    }

    @Test
    void generateReturns201WithValidRequest() throws Exception {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);
        Coupon coupon = new Coupon(promotion(), "STUDENT20", new BigDecimal("20.00"), expiresAt);
        ReflectionTestUtils.setField(coupon, "id", 5L);
        when(couponService.generate(1L, "STUDENT20", new BigDecimal("20.00"), expiresAt)).thenReturn(coupon);

        mockMvc.perform(post("/api/promotions/1/coupons")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GenerateCouponRequest("STUDENT20", new BigDecimal("20.00"), expiresAt))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("STUDENT20"));
    }

    @Test
    void generateReturns400WhenExpiresAtIsInThePast() throws Exception {
        OffsetDateTime pastDate = OffsetDateTime.now().minusDays(1);

        mockMvc.perform(post("/api/promotions/1/coupons")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GenerateCouponRequest("EXPIRED10", new BigDecimal("10.00"), pastDate))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.expiresAt").exists());
    }

    @Test
    void generateReturns409WhenCodeAlreadyExists() throws Exception {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);
        when(couponService.generate(1L, "STUDENT20", new BigDecimal("20.00"), expiresAt))
                .thenThrow(DuplicateResourceException.of("un cupón", "code", "STUDENT20"));

        mockMvc.perform(post("/api/promotions/1/coupons")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new GenerateCouponRequest("STUDENT20", new BigDecimal("20.00"), expiresAt))))
                .andExpect(status().isConflict());
    }
}