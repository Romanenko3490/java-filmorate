package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.film.Director;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DirectorRepository {

    Optional<Director> findById(long directorId);

    List<Director> findAll();

    Director add(Director director);

    Director update(Director director);

    void delete(long id);

    boolean existAllByIds(Set<Long> ids);

}
