package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
public class FilmRepository extends BaseRepository<Film> implements FilmStorage {

    // region SQL Queries - Basic Film Operations
    private static final String GET_ALL_FILMS_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description " +
                    "FROM films f LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id";

    private static final String GET_FILM_BY_ID_QUERY = GET_ALL_FILMS_QUERY + " WHERE f.film_id = ?";
    private static final String INSERT_FILM_QUERY =
            "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                    "duration = ?, mpa_rating_id = ? WHERE film_id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";
    // endregion

    // region SQL Queries - Genres
    private static final String INSERT_FILM_GENRE_QUERY =
            "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES_QUERY =
            "DELETE FROM film_genre WHERE film_id = ?";
    private static final String GET_FILM_GENRES_QUERY =
            "SELECT g.genre_id, g.name FROM film_genre fg " +
                    "JOIN genre g ON fg.genre_id = g.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.genre_id";
    // endregion

    // region SQL Queries - Likes
    private static final String INSERT_FILM_LIKE_QUERY =
            "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_FILM_LIKE_QUERY =
            "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    // endregion

    // region SQL Queries - Special Operations
    private static final String GET_POPULAR_FILMS_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "WHERE EXISTS (SELECT 1 FROM film_likes WHERE film_id = f.film_id) " +
                    "ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_id = f.film_id) DESC " +
                    "LIMIT ?";
    private static final String CHECK_MPA_EXISTS_QUERY =
            "SELECT COUNT(*) FROM mpa_rating WHERE mpa_id = ?";
    // endregion

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    // region Basic Film CRUD Operations
    @Override
    public Collection<Film> getFilms() {
        List<Film> films = jdbc.query(GET_ALL_FILMS_QUERY, mapper);
        loadGenresForFilms(films);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        try {
            Film film = jdbc.queryForObject(GET_FILM_BY_ID_QUERY, mapper, id);
            if (film != null) {
                loadGenresForFilm(film);
            }
            return Optional.ofNullable(film);
        } catch (Exception e) {
            log.warn("Film not found with id: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("mpa_rating_id", film.getMpa().getId());

        long filmId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        film.setId(filmId);
        updateFilmGenres(film);
        return film;
    }

    @Override
    @Transactional
    public Film updateFilm(Film film) {
        // Сначала проверяем существование фильма
        if (!filmExists(film.getId())) {
            throw new NotFoundException("Film not found with id: " + film.getId());
        }

        int updated = jdbc.update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Film not found with id: " + film.getId());
        }

        updateFilmGenres(film);
        return film;
    }

    private boolean filmExists(long id) {
        String query = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteFilm(long id) {
        jdbc.update(DELETE_FILM_QUERY, id);
    }


    // endregion

    // region Additional Film Operations
    @Override
    public Map<Long, Film> getFilmsMap() {
        Collection<Film> films = getFilms();
        Map<Long, Film> filmMap = new HashMap<>();
        films.forEach(film -> filmMap.put(film.getId(), film));
        return filmMap;
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = (count != null && count > 0) ? count : 10;
        log.debug("Executing popular films query with limit: {}", limit);
        List<Film> films = jdbc.query(GET_POPULAR_FILMS_QUERY, mapper, limit);
        log.debug("Found {} films with likes", films.size());
        loadGenresForFilms(films);
        return films;
    }
    // endregion

    // region Like Operations
    public void addLike(long filmId, long userId) {
        try {
            jdbc.update(INSERT_FILM_LIKE_QUERY, filmId, userId);
            log.info("Like added - filmId: {}, userId: {}", filmId, userId);
        } catch (Exception e) {
            log.error("Error adding like", e);
            throw new RuntimeException("Failed to add like");
        }
    }

    public void removeLike(long filmId, long userId) {
        int rowsDeleted = jdbc.update(DELETE_FILM_LIKE_QUERY, filmId, userId);
        log.debug("Deleted {} rows for filmId {} and userId {}", rowsDeleted, filmId, userId);
        if (rowsDeleted == 0) {
            throw new NotFoundException(String.format(
                    "Like not found for filmId %d and userId %d", filmId, userId));
        }
    }
    // endregion

    // region Genre Operations
    private void updateFilmGenres(Film film) {
        jdbc.update(DELETE_FILM_GENRES_QUERY, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> batchArgs = film.getGenres().stream()
                    .map(genre -> new Object[]{film.getId(), genre.getId()})
                    .collect(Collectors.toList());

            jdbc.batchUpdate(INSERT_FILM_GENRE_QUERY, batchArgs);
        }
    }

    private void loadGenresForFilm(Film film) {
        List<Genre> genres = jdbc.query(GET_FILM_GENRES_QUERY, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(new HashSet<>(genres));
    }

    private void loadGenresForFilms(Collection<Film> films) {
        Map<Long, Film> filmMap = new HashMap<>();
        films.forEach(film -> filmMap.put(film.getId(), film));

        if (filmMap.isEmpty()) {
            return;
        }

        String inClause = String.join(",", Collections.nCopies(filmMap.size(), "?"));
        String query = "SELECT fg.film_id, g.genre_id, g.name FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (" + inClause + ") ORDER BY g.genre_id";

        jdbc.query(query, rs -> {
            long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("name"));
                film.getGenres().add(genre);
            }
        }, filmMap.keySet().toArray());
    }
    // endregion

    // region MPA Operations
    public boolean mpaExists(int mpaId) {
        Integer count = jdbc.queryForObject(CHECK_MPA_EXISTS_QUERY, Integer.class, mpaId);
        return count != null && count > 0;
    }
    // endregion


}