package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private Map<Long, Film> films = new HashMap<>();

    public Collection<Film> getFilms() {
        return films.values();
    }

    public Optional<Film> getFilmById(long id) {
        log.info("Get film by id: {}", id);
        return Optional.ofNullable(films.get(id))
                .map(film -> {
                    log.info("Get film by id: {}", id);
                    return film;
                })
                .or(() -> {
                    log.info("No film with id {}", id);
                    throw new NotFoundException("No film with id " + id);
                });

    }

    public Film addFilm(Film newFilm) {
        if (newFilm == null) {
            log.error("Attempt to add null film");
            throw new ValidationException("Film is null");
        }
        try {
            log.info("Adding new film: filmName={}", newFilm.getName());
            newFilm.setId(getNextFilmId());
            films.put(newFilm.getId(), newFilm);
            log.info("Film added: filmId={}, filmName={}", newFilm.getId(), newFilm.getName());
            return newFilm;
        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw e;
        }
    }


    public Film updateFilm(Film newFilm) {
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

    public Map<Long, Film> getFilmsMap() {
        return films;
    }


    private long getNextFilmId() {
        long maxId = films.values().stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }


}
