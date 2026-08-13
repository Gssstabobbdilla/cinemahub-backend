package com.cinemahub.cinemahub.movie.controller;

import jakarta.validation.Valid;
import com.cinemahub.cinemahub.movie.dto.ClassificationRequest;
import com.cinemahub.cinemahub.movie.dto.ClassificationResponse;
import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.service.ClassificationService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/classifications")
public class ClassificationController {

    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @GetMapping
    public List<ClassificationResponse> findAll() {
        return classificationService.findAll().stream().map(ClassificationResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClassificationResponse findById(@PathVariable Long id) {
        return ClassificationResponse.from(classificationService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassificationResponse create(@Valid @RequestBody ClassificationRequest request) {
        Classification classification = classificationService.create(request.code(), request.description());
        return ClassificationResponse.from(classification);
    }

    @PutMapping("/{id}")
    public ClassificationResponse update(@PathVariable Long id, @Valid @RequestBody ClassificationRequest request) {
        Classification classification = classificationService.update(id, request.code(), request.description());
        return ClassificationResponse.from(classification);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        classificationService.delete(id);
    }
}