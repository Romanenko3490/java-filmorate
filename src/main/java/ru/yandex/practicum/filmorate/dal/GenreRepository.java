package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreRepository {
    boolean existsById(int genreId);  // Проверка существования жанра
    Optional<Genre> findById(int genreId);  // Получение жанра по ID
    List<Genre> findAll();
}
