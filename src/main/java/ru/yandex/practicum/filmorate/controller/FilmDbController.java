package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/{filmId}")
    public FilmDto getFilmById(@PathVariable long filmId) {
        return filmDbService.getFilmById(filmId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(
            @RequestParam(defaultValue = "10") Integer count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        List<FilmDto> result = filmDbService.getMostPopularFilms(count, genreId, year);
        if (result.isEmpty()) {
            log.warn("Popular films request returned empty list. Check if any films match the criteria.");
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

    @PutMapping("/{filmId}/like/{userId}")
    public FilmDto likeFilm(@PathVariable long filmId,
                            @PathVariable long userId) {
        return filmDbService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto deleteLike(@PathVariable long id,
                              @PathVariable long userId) {
        return filmDbService.removeLike(id, userId);
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable long filmId) {
        filmDbService.removeFilm(filmId);
    }
}