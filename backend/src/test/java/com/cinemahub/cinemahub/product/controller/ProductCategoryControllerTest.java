package com.cinemahub.cinemahub.product.controller;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.product.dto.ProductCategoryRequest;
import com.cinemahub.cinemahub.product.entity.ProductCategory;
import com.cinemahub.cinemahub.product.service.ProductCategoryService;

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

@WebMvcTest(ProductCategoryController.class)
class ProductCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private ProductCategoryService productCategoryService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        ProductCategory category = new ProductCategory("Snacks");
        ReflectionTestUtils.setField(category, "id", 1L);
        when(productCategoryService.create("Snacks")).thenReturn(category);

        mockMvc.perform(post("/api/product-categories")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductCategoryRequest("Snacks"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Snacks"));
    }

    @Test
    void createReturns400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/product-categories")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductCategoryRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void createReturns409WhenNameAlreadyExists() throws Exception {
        when(productCategoryService.create("Snacks"))
                .thenThrow(DuplicateResourceException.of("una categoría", "name", "Snacks"));

        mockMvc.perform(post("/api/product-categories")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductCategoryRequest("Snacks"))))
                .andExpect(status().isConflict());
    }
}