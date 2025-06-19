package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Map;

@Component
public interface FilmStorage {

    Collection<Film> getFilms();

    Film getFilmById(int id);

    Film addFilm(Film newFilm);

    Film updateFilm(Film newFilm);

    Map<Integer, Film> getFilmsMap();

}
