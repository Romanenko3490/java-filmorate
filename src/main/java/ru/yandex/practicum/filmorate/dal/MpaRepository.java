package ru.yandex.practicum.filmorate.dal;


import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MpaRepository {
    boolean existsById(Long mpaId);

    Optional<MpaRating> findById(Long mpaId);

    List<MpaRating> findAll();

    public Set<Long> findAllExistingIds(Set<Long> ids);
}