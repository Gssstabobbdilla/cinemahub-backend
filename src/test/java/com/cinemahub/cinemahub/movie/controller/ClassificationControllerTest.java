package com.cinemahub.cinemahub.movie.controller;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.movie.dto.ClassificationRequest;
import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.service.ClassificationService;

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

@WebMvcTest(ClassificationController.class)
class ClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

   @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private ClassificationService classificationService;

    @Test
    void createReturns201WithValidRequest() throws Exception {
        Classification classification = new Classification("PG-13", "Supervisión de adultos");
        ReflectionTestUtils.setField(classification, "id", 1L);
        when(classificationService.create("PG-13", "Supervisión de adultos")).thenReturn(classification);

        mockMvc.perform(post("/api/classifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ClassificationRequest("PG-13", "Supervisión de adultos"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PG-13"));
    }

    @Test
    void createReturns400WhenCodeIsBlank() throws Exception {
        mockMvc.perform(post("/api/classifications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ClassificationRequest("", "desc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.code").exists());
    }

    @Test
    void findByIdReturns404WhenNotFound() throws Exception {
        when(classificationService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Classification no encontrado: id=99"));

        mockMvc.perform(get("/api/classifications/99"))
                .andExpect(status().isNotFound());
    }
}