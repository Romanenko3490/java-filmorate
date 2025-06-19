package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private Map<Integer, Film> films = new HashMap<>();

    public Collection<Film> getFilms() {
        return films.values();
    }

    public Film getFilmById(int id) {
        log.info("Get film by id: {}", id);
        if (!films.containsKey(id)) {
            log.error("Film not found by id: {}", id);
            throw new NotFoundException("Film not found by id: " + id);
        }
        return films.get(id);
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

    public Map<Integer, Film> getFilmsMap() {
        return films;
    }


    private int getNextFilmId() {
        int maxId = films.values().stream()
                .mapToInt(Film::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }


}
