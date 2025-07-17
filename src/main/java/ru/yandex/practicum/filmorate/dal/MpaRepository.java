package ru.yandex.practicum.filmorate.dal;


import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.util.List;
import java.util.Optional;

public interface MpaRepository {
    boolean existsById(int mpaId);        // Проверка существования MPA по ID
    Optional<MpaRating> findById(int mpaId); // Получение MPA (пригодится для других методов)
    List<MpaRating> findAll();
}