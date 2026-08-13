package com.cinemahub.cinemahub.movie.service;

import com.cinemahub.cinemahub.common.exception.DuplicateResourceException;
import com.cinemahub.cinemahub.common.exception.ResourceNotFoundException;
import com.cinemahub.cinemahub.movie.entity.Genre;
import com.cinemahub.cinemahub.movie.repository.GenreRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    public Genre findById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Genre", id));
    }

    @Transactional
    public Genre create(String name) {
        genreRepository.findByName(name).ifPresent(existing -> {
            throw DuplicateResourceException.of("un género", "name", name);
        });
        return genreRepository.save(new Genre(name));
    }

    @Transactional
    public Genre update(Long id, String name) {
        Genre genre = findById(id);
        if (!genre.getName().equals(name)) {
            genreRepository.findByName(name).ifPresent(existing -> {
                throw DuplicateResourceException.of("un género", "name", name);
            });
        }
        genre.setName(name);
        return genre;
    }

    @Transactional
    public void delete(Long id) {
        genreRepository.delete(findById(id));
    }
}