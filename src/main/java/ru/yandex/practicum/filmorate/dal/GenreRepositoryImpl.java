package ru.yandex.practicum.filmorate.dal;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreRepositoryImpl implements GenreRepository {
    private final JdbcTemplate jdbc;

    @Override
    public boolean existsById(int genreId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM genre WHERE genre_id = ?)";
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, genreId));
    }

    @Override
    public Optional<Genre> findById(int genreId) {
        log.debug("Getting genre by id: {}", genreId);
        String sql = "SELECT * FROM genre WHERE genre_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, (rs, rowNum) ->
                            new Genre(rs.getInt("genre_id"), rs.getString("name"))
                    , genreId));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Genre not found with id: {}", genreId);
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> findAll() {
        log.debug("Getting all genres");
        String sql = "SELECT * FROM genre ORDER BY genre_id";
        return jdbc.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name")));
    }
}
