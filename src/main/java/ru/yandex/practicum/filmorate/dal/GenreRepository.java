package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository {
    boolean existsById(int genreId);

    Optional<Genre> findById(int genreId);

    List<Genre> findAll();

    Set<Integer> findAllExistingIds(Set<Integer> ids);

}
