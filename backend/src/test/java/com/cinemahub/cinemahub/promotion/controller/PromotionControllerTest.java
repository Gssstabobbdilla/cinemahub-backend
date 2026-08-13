package com.cinemahub.cinemahub.promotion.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.promotion.dto.CreatePromotionRequest;
import com.cinemahub.cinemahub.promotion.entity.Promotion;
import com.cinemahub.cinemahub.promotion.service.PromotionService;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromotionController.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private PromotionService promotionService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(1);
        Promotion promotion = new Promotion("2x1 en miércoles", new BigDecimal("50.00"), start, end);
        ReflectionTestUtils.setField(promotion, "id", 1L);
        when(promotionService.create("2x1 en miércoles", new BigDecimal("50.00"), start, end))
                .thenReturn(promotion);

        mockMvc.perform(post("/api/promotions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePromotionRequest("2x1 en miércoles", new BigDecimal("50.00"), start, end))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("2x1 en miércoles"));
    }

    @Test
    void createReturns400WhenDiscountPercentageExceeds100() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(1);

        mockMvc.perform(post("/api/promotions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreatePromotionRequest("Promo inválida", new BigDecimal("150.00"), start, end))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.discountPercentage").exists());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(promotionService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Promotion no encontrado: id=99"));

        mockMvc.perform(get("/api/promotions/99"))
                .andExpect(status().isNotFound());
    }
}