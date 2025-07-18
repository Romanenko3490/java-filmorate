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
    private static final String EXISTS_BY_ID_QUERY =
            "SELECT EXISTS(SELECT 1 FROM genre WHERE genre_id = ?)";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM genre WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM genre ORDER BY genre_id";

    private final JdbcTemplate jdbc;

    @Override
    public boolean existsById(int genreId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(EXISTS_BY_ID_QUERY, Boolean.class, genreId));
    }

    @Override
    public Optional<Genre> findById(int genreId) {
        log.debug("Getting genre by id: {}", genreId);
        try {
            return Optional.ofNullable(jdbc.queryForObject(
                    FIND_BY_ID_QUERY,
                    (rs, rowNum) -> new Genre(rs.getInt("genre_id"), rs.getString("name")),
                    genreId
            ));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Genre not found with id: {}", genreId);
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> findAll() {
        log.debug("Getting all genres");
        return jdbc.query(
                FIND_ALL_QUERY,
                (rs, rowNum) -> new Genre(rs.getInt("genre_id"), rs.getString("name"))
        );
    }
}