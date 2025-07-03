package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;
import java.util.Map;

public interface FilmStorage {

    Collection<Film> getFilms();

    Film getFilmById(int id);

    Film addFilm(Film newFilm);

    Film updateFilm(Film newFilm);

    Map<Integer, Film> getFilmsMap();

}
