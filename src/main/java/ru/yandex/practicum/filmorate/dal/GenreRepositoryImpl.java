package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.*;

@Slf4j
@Repository
public class GenreRepositoryImpl extends BaseRepository<Genre> implements GenreRepository {
    private static final String EXISTS_BY_ID_QUERY =
            "SELECT EXISTS(SELECT 1 FROM genre WHERE genre_id = ?)";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM genre WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM genre ORDER BY genre_id";
    private static final String FIND_EXISTING_IDS_BASE_QUERY =
            "SELECT genre_id FROM genre WHERE genre_id IN (%s)";
    private static final String DELETE_FILM_GENRES_QUERY =
            "DELETE FROM film_genre WHERE film_id = ?";


    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) ->
            new Genre(rs.getInt("genre_id"), rs.getString("name"));

    public GenreRepositoryImpl(JdbcTemplate jdbc, Checker checker) {
        super(jdbc, (rs, rowNum)
                -> new Genre(rs.getInt("genre_id"),
                rs.getString("name")),checker);
    }

    @Override
    public boolean existsById(int genreId) {
        return existsById(EXISTS_BY_ID_QUERY, genreId);
    }

    @Override
    public Optional<Genre> findById(int genreId) {
        log.debug("Getting genre by id: {}", genreId);
        return super.findById(FIND_BY_ID_QUERY, genreId);
    }

    @Override
    public List<Genre> findAll() {
        log.debug("Getting all genres");
        return super.findAll(FIND_ALL_QUERY);
    }

    @Override
    public Set<Long> findAllExistingIds(Set<Long> ids) {
        return super.findAllExistingIds(FIND_EXISTING_IDS_BASE_QUERY, ids);
    }

}