package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.zalando.logbook.attributes.AttributeExtractor;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Qualifier("inMemoryFilmService")
@Deprecated
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final AttributeExtractor getAttributeExtractor;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage, AttributeExtractor getAttributeExtractor) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.getAttributeExtractor = getAttributeExtractor;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getFilms();
    }

    public Film add(Film film) {

        return filmStorage.addFilm(film);
    }

    public Film getById(Long id) {
        return filmStorage.getFilmById(id).get();
    }

    public Film update(@Valid Film newFilm) {
        if (newFilm == null) {
            log.error("Film is null");
            throw new ValidationException("Film is null");
        }

        if (newFilm.getId() == null) {
            log.error("Film id is null");
            throw new ValidationException("Film id cannot be null");
        }

        try {
            if (!filmStorage.getFilmsMap().containsKey(newFilm.getId())) {
                throw new NotFoundException("Film not found by id " + newFilm.getId());
            }
            log.info("Updating film: filmName={}", newFilm.getName());
            return filmStorage.updateFilm(newFilm);

        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw e;
        } catch (NotFoundException e) {
            log.error("Film not found by id {}", newFilm.getId());
            throw e;
        }
    }

    public void addLike(Long filmId, Long userId) {

        isPresentInStorage(filmId, userId);

        Film film = filmStorage.getFilmById(filmId).get();
        film.getLikes().add(userId);
        log.info("Пользователь id " + userId + " лайкнул фильм id " + filmId);
    }

    public void removeLike(Long filmId, Long userId) {

        isPresentInStorage(filmId, userId);

        Film film = filmStorage.getFilmById(filmId).get();
        film.getLikes().remove(userId);
        log.info("Пользователь id " + userId + " Удалил лайк с фильма id " + filmId);
    }

    public List<Film> getMostPopularFilms(Integer count) {

        log.debug("Getting {} most popular films", count);

        if (filmStorage.getFilms().isEmpty()) {
            log.info("No films found");
            return Collections.emptyList();
        }

        return filmStorage.getFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikesCount(), f1.getLikesCount()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void isPresentInStorage(Long filmId, Long userId) {
        if (!filmStorage.getFilmsMap().containsKey(filmId)) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        if (!userStorage.getUsersMap().containsKey(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }
    }
}
