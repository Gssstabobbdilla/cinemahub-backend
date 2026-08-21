package com.cinemahub.cinemahub.product.controller;

import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.product.dto.CreateProductRequest;
import com.cinemahub.cinemahub.product.entity.Product;
import com.cinemahub.cinemahub.product.entity.ProductCategory;
import com.cinemahub.cinemahub.product.service.ProductService;
import com.cinemahub.cinemahub.product.dto.UpdateProductRequest;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


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
        when(productService.create(1L, "Nachos", new BigDecimal("12.00"),
         "https://www.google.com/imgres?q=nachos&imgurl=https%3A%2F%2Fassets.tmecosys.com%2Fimage%2Fupload%2Ft_web_rdp_recipe_584x480%2Fimg%2Frecipe%2Fras%2FAssets%2F7695121e-8b9a-4d00-ab96-4430e47266ba%2FDerivates%2F445ffdd9-9a8e-48fa-9e86-84c1e94469ca.jpg&imgrefurl=https%3A%2F%2Fcookidoo.mx%2Frecipes%2Frecipe%2Fes-MX%2Fr735813&docid=r7jhZCpXS-AuaM&tbnid=CI4CR_bYBB75DM&vet=12ahUKEwjr_4TXnrCWAxUpPbkGHcw8PX4QnPAOegQINRAA..i&w=584&h=480&hcb=2&ved=2ahUKEwjr_4TXnrCWAxUpPbkGHcw8PX4QnPAOegQINRAA")).thenReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProductRequest(1L, "Nachos", new BigDecimal("12.00"), 
                            "https://www.google.com/imgres?q=nachos&imgurl=https%3A%2F%2Fassets.tmecosys.com%2Fimage%2Fupload%2Ft_web_rdp_recipe_584x480%2Fimg%2Frecipe%2Fras%2FAssets%2F7695121e-8b9a-4d00-ab96-4430e47266ba%2FDerivates%2F445ffdd9-9a8e-48fa-9e86-84c1e94469ca.jpg&imgrefurl=https%3A%2F%2Fcookidoo.mx%2Frecipes%2Frecipe%2Fes-MX%2Fr735813&docid=r7jhZCpXS-AuaM&tbnid=CI4CR_bYBB75DM&vet=12ahUKEwjr_4TXnrCWAxUpPbkGHcw8PX4QnPAOegQINRAA..i&w=584&h=480&hcb=2&ved=2ahUKEwjr_4TXnrCWAxUpPbkGHcw8PX4QnPAOegQINRAA"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nachos"))
                .andExpect(jsonPath("$.categoryName").value("Snacks"))
                .andExpect(jsonPath("$.imageUrl").value("https://www.google.com/imgres?q=nachos&imgurl=https%3A%2F%2Fassets.tmecosys.com%2Fimage%2Fupload%2Ft_web_rdp_recipe_584x480%2Fimg%2Frecipe%2Fras%2FAssets%2F7695121e-8b9a-4d00-ab96-4430e47266ba%2FDerivates%2F445ffdd9-9a8e-48fa-9e86-84c1e94469ca.jpg&imgrefurl=https%3A%2F%2Fcookidoo.mx%2Frecipes%2Frecipe%2Fes-MX%2Fr735813&docid=r7jhZCpXS-AuaM&tbnid=CI4CR_bYBB75DM&vet=12ahUKEwjr_4TXnrCWAxUpPbkGHcw8PX4QnPAOegQINRAA..i&w=584&h=480&hcb=2&ved=2ahUKEwjr_4TXnrCWAxUpPbkGHcw8PX4QnPAOegQINRAA"));
    }

    @Test
    void createReturns400WhenPriceIsNegative() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateProductRequest(1L, "Nachos", new BigDecimal("-12.00"),
                            "https://www.google.com/imgres?q=nachos&imgurl=https%3A%2F%2Fassets.tmecosys.com%2Fimage%2Fupload%2Ft_web_rdp_recipe_584x480%2Fimg%2Frecipe%2Fras%2FAssets%2F7695121e-8b9a-4d00-ab96-4430e47266ba%2FDerivates%2F445ffdd9-9a8e-48fa-9e86-84c1e94469ca.jpg&imgrefurl=https%3A%2F%2Fcookidoo.mx%2Frecipes%2Frecipe%2Fes-MX%2Fr735813&docid=r7jhZCpXS-AuaM&tbnid=CI4CR_bYBB75DM&vet=12ahUKEwjr_4TXnrCWAxUpPbkGHcw8PX4QnPAOegQINRAA..i&w=584&h=480&hcb=2&ved=2ahUKEwjr_4TXnrCWAxUpPbkGHcw8PX4QnPAOegQINRAA"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.price").exists());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(productService.findById(99L)).thenThrow(new ResourceNotFoundException("Product no encontrado: id=99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
void updateReturns200WithValidRequest() throws Exception {
    ProductCategory category = new ProductCategory("Snacks");
    ReflectionTestUtils.setField(category, "id", 1L);
    Product product = new Product(category, "Nachos Grande", new BigDecimal("15.00"));
    ReflectionTestUtils.setField(product, "id", 5L);
    when(productService.update(5L, "Nachos Grande", new BigDecimal("15.00"), "https://x.com/img.png", "Con queso"))
            .thenReturn(product);

    mockMvc.perform(put("/api/products/5")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new UpdateProductRequest("Nachos Grande", new BigDecimal("15.00"), "https://x.com/img.png", "Con queso"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Nachos Grande"))
            .andExpect(jsonPath("$.price").value(15.00));
}

@Test
void updateReturns400WhenNameIsBlank() throws Exception {
    mockMvc.perform(put("/api/products/5")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new UpdateProductRequest("", new BigDecimal("15.00"), null, null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields.name").exists());
}

@Test
void updateReturns404WhenProductNotFound() throws Exception {
    when(productService.update(99L, "X", new BigDecimal("10.00"), null, null))
            .thenThrow(new ResourceNotFoundException("Product no encontrado: id=99"));

    mockMvc.perform(put("/api/products/99")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new UpdateProductRequest("X", new BigDecimal("10.00"), null, null))))
            .andExpect(status().isNotFound());
}
}