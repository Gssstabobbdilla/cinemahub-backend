package com.cinemahub.cinemahub.movie.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.movie.entity.Classification;
import com.cinemahub.cinemahub.movie.repository.ClassificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClassificationService {

    private final ClassificationRepository classificationRepository;

    public ClassificationService(ClassificationRepository classificationRepository) {
        this.classificationRepository = classificationRepository;
    }

    public List<Classification> findAll() {
        return classificationRepository.findAll();
    }

    public Classification findById(Long id) {
        return classificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Classification", id));
    }

    @Transactional
    public Classification create(String code, String description) {
        classificationRepository.findByCode(code).ifPresent(existing -> {
            throw DuplicateResourceException.of("una clasificación", "code", code);
        });
        return classificationRepository.save(new Classification(code, description));
    }

    @Transactional
    public Classification update(Long id, String code, String description) {
        Classification classification = findById(id);
        if (!classification.getCode().equals(code)) {
            classificationRepository.findByCode(code).ifPresent(existing -> {
                throw DuplicateResourceException.of("una clasificación", "code", code);
            });
        }
        classification.setCode(code);
        classification.setDescription(description);
        return classification;
    }

    @Transactional
    public void delete(Long id) {
        classificationRepository.delete(findById(id));
    }
}