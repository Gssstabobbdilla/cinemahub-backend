package com.cinemahub.cinemahub.product.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.product.dto.CreateProductRequest;
import com.cinemahub.cinemahub.product.entity.Product;
import com.cinemahub.cinemahub.product.entity.ProductCategory;
import com.cinemahub.cinemahub.product.service.ProductService;

import tools.jackson.databind.json.JsonMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        ProductCategory category = new ProductCategory("Snacks");
        ReflectionTestUtils.setField(category, "id", 1L);
        Product product = new Product(category, "Nachos", new BigDecimal("12.00"));
        ReflectionTestUtils.setField(product, "id", 5L);
        when(productService.create(1L, "Nachos", new BigDecimal("12.00"))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProductRequest(1L, "Nachos", new BigDecimal("12.00")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nachos"))
                .andExpect(jsonPath("$.categoryName").value("Snacks"));
    }

    @Test
    void createReturns400WhenPriceIsNegative() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProductRequest(1L, "Nachos", new BigDecimal("-12.00")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.price").exists());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(productService.findById(99L)).thenThrow(new ResourceNotFoundException("Product no encontrado: id=99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }
}