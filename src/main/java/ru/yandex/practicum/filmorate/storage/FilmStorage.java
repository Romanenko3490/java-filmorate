package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> getFilms();

    Optional<Film> getFilmById(long id);

    Film addFilm(Film newFilm);

    Film updateFilm(Film newFilm);

    Map<Long, Film> getFilmsMap();

}
