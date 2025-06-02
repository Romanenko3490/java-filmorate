package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@RequestBody Film newFilm) {
        if (newFilm == null) {
            log.error("Attempt to add null film");
            throw new ValidationException("Film is null");
        }
        try {
            log.info("Adding new film: filmName={}", newFilm.getName());
            validateFilm(newFilm);
            newFilm.setId(getNextFilmId());
            films.put(newFilm.getId(), newFilm);
            log.info("Film added: filmId={}, filmName={}", newFilm.getId(), newFilm.getName());
            return newFilm;
        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw e;
        }
    }


    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        if (newFilm == null) {
            log.error("Film is null");
            throw new ValidationException("Film is null");
        }

        try {
            log.info("Updating film: filmName={}", newFilm.getName());
            if (newFilm.getId() == null) {
                log.error("Film id is null");
                throw new ValidationException("Film id is null");
            }

            validateFilm(newFilm);
            if (films.containsKey(newFilm.getId())) {
                Film oldFilm = films.get(newFilm.getId());
                if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                    oldFilm.setName(newFilm.getName());
                }
                if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
                    oldFilm.setDescription(newFilm.getDescription());
                }
                if (newFilm.getReleaseDate() != null) {
                    oldFilm.setReleaseDate(newFilm.getReleaseDate());
                }
                if (newFilm.getDuration() != null) {
                    oldFilm.setDuration(newFilm.getDuration());
                }
                log.info("Film name={} with id={} is updated", oldFilm.getName(), oldFilm.getId());
                return oldFilm;
            }
        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw e;
        }
        log.error("Film id={} not found", newFilm.getId());
        throw new NotFoundException("Film with id " + newFilm.getId() + " not found");
    }


    private int getNextFilmId() {
        int maxId = films.values().stream()
                .mapToInt(Film::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }

    private void validateFilm(Film newFilm) {
        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            log.error("Film name is empty");
            throw new ValidationException("Film name cannot be empty");
        }
        if (newFilm.getDescription().length() > 200) {
            log.error("Film description is too long");
            throw new ValidationException("Film description cannot be longer than 200 characters");
        }
        LocalDate existingDate = LocalDate.of(1895, 12, 28);
        if (newFilm.getReleaseDate().isBefore(existingDate)) {
            log.error("Film release date is before existingDate(28.12.1895)");
            throw new ValidationException("Film release date cannot be before existing(28.12.1895)");
        }
        if (newFilm.getDuration() <= 0) {
            log.error("Film duration is negative or zero");
            throw new ValidationException("Film duration cannot be negative or zero");
        }
    }
}

