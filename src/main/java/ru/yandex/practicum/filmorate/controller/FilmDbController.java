package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmDbService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/films")
public class FilmDbController {

    private final FilmDbService filmDbService;

    @Autowired
    public FilmDbController(FilmDbService filmDbService) {
        this.filmDbService = filmDbService;
    }

    @GetMapping
    public Collection<FilmDto> getFilms() {
        return filmDbService.getAllFilms();
    }

    @GetMapping("/{film_id}")
    public FilmDto getFilmById(@PathVariable long film_id) {
        return filmDbService.getFilmById(film_id);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(
            @RequestParam(required = false) Integer count) {
        List<FilmDto> result = filmDbService.getMostPopularFilms(count);
        if (result.isEmpty()) {
            log.warn("Popular films request returned empty list. Check if any films have likes.");
        }
        return result;
    }

    @PostMapping
    public FilmDto addFilm(@Valid @RequestBody NewFilmRequest newFilm) {
        if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Release date must be after 1895-12-28");
        }
        return filmDbService.addFilm(newFilm);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody UpdateFilmRequest request) {
        if (request.getId() == null) {
            throw new ValidationException("Film ID must not be null");
        }
        return filmDbService.updateFilm(request.getId(), request);
    }

    @PutMapping("/{film_id}/like/{userId}")
    public FilmDto likeFilm(@PathVariable long film_id,
                            @PathVariable long userId) {
        return filmDbService.addLike(film_id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto deleteLike(@PathVariable long id,
                              @PathVariable long userId) {
        return filmDbService.removeLike(id, userId);
    }
}