package ru.yandex.practicum.filmorate.dal;


import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.util.List;
import java.util.Optional;

public interface MpaRepository {
    boolean existsById(Long mpaId);

    Optional<MpaRating> findById(Long mpaId);

    List<MpaRating> findAll();
}