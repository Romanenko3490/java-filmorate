package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
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

    private static final String INSERT_FILM_DIRECTOR_QUERY =
            "INSERT INTO film_directors (film_id, id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTOR_QUERY =
            "DELETE FROM film_directors WHERE film_id = ?";

    // region SQL Queries - Likes
    private static final String INSERT_FILM_LIKE_QUERY =
            "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_FILM_LIKE_QUERY =
            "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    // endregion

    // region SQL Queries - Special Operations
    private static final String GET_POPULAR_FILMS_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description, " +
                    "(SELECT COUNT(*) FROM film_likes WHERE film_id = f.film_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "ORDER BY likes_count DESC, f.film_id " +
                    "LIMIT ?";

    private static final String CHECK_MPA_EXISTS_QUERY =
            "SELECT COUNT(*) FROM mpa_rating WHERE mpa_id = ?";

    private static final String GET_POPULAR_FILMS_BY_GENRE_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description, " +
                    "(SELECT COUNT(*) FROM film_likes WHERE film_id = f.film_id) AS likes_count " +
                    "FROM films f " +
                    "JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "WHERE f.film_id IN (SELECT fg.film_id FROM film_genre fg WHERE fg.genre_id = ?) " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    private static final String GET_POPULAR_FILMS_BY_GENRE_AND_YEAR_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description, " +
                    "(SELECT COUNT(*) FROM film_likes WHERE film_id = f.film_id) AS likes_count " +
                    "FROM films f " +
                    "JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "WHERE f.film_id IN (SELECT fg.film_id FROM film_genre fg WHERE fg.genre_id = ?) " +
                    "AND EXTRACT(YEAR FROM f.release_date) = ? " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    private static final String GET_POPULAR_FILMS_BY_YEAR_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description, " +
                    "(SELECT COUNT(*) FROM film_likes WHERE film_id = f.film_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "WHERE EXTRACT(YEAR FROM f.release_date) = ? " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    // endregion


    // region SQL Queries - Search Operations
    private static final String SEARCH_FILMS_BY_TITLE_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "WHERE LOWER(f.name) LIKE LOWER(?) " +
                    "ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_likes.film_id = f.film_id) DESC, f.film_id";

    private static final String SEARCH_FILMS_BY_DIRECTOR_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "WHERE EXISTS (" +
                    "    SELECT 1 FROM film_directors fd " +
                    "    JOIN directors d ON fd.id = d.id " +
                    "    WHERE fd.film_id = f.film_id " +
                    "    AND LOWER(d.name) LIKE LOWER(?)" +
                    ") " +
                    "ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_likes.film_id = f.film_id) DESC, f.film_id";

    private static final String SEARCH_FILMS_BY_TITLE_AND_DIRECTOR_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "WHERE LOWER(f.name) LIKE LOWER(?) " +
                    "   OR EXISTS (" +
                    "       SELECT 1 FROM film_directors fd " +
                    "       JOIN directors d ON fd.id = d.id " +
                    "       WHERE fd.film_id = f.film_id " +
                    "       AND LOWER(d.name) LIKE LOWER(?)" +
                    "   ) " +
                    "ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_likes.film_id = f.film_id) DESC, f.film_id";
    // endregion


    //Director Query Region

    private static final String GET_FILM_DIRECTORS_QUERY =
            "SELECT d.id, d.name FROM film_directors fd " +
                    "JOIN directors d ON fd.id = d.id " +
                    "WHERE fd.film_id = ? ORDER BY d.id";

    private static final String GET_FILMS_BY_DIRECTOR_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.mpa_id, m.mpa_name, m.description AS mpa_description, " +
                    "COUNT(fl.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.id = ? " +
                    "GROUP BY f.film_id, m.mpa_id " +
                    "ORDER BY %s";
    //endregion

    //Recommendations Query Region
    private static final String GET_ALL_USER_LIKES =
            "SELECT user_id, film_id FROM film_likes";
    private static final String GET_USER_LIKED_FILMS =
            "SELECT film_id FROM film_likes WHERE user_id = ?";
    //endregion

    // region SQL Queries - Common Films Query
    private static final String GET_COMMON_FILMS_QUERY =
            "SELECT f.*, m.mpa_id, m.mpa_name, m.description AS mpa_description " +
                    "FROM films f " +
                    "JOIN mpa_rating m ON f.mpa_rating_id = m.mpa_id " +
                    "WHERE EXISTS (SELECT 1 FROM film_likes WHERE user_id = ? AND film_id = f.film_id) " +
                    "AND EXISTS (SELECT 1 FROM film_likes WHERE user_id = ? AND film_id = f.film_id) " +
                    "ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_id = f.film_id) DESC";
    //endregion

    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper, Checker checker) {
        super(jdbc, mapper, checker);
    }

    // region Basic Film CRUD Operations
    @Override
    public Collection<Film> getFilms() {
        List<Film> films = jdbc.query(GET_ALL_FILMS_QUERY, mapper);
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(long id) {
        try {
            Film film = jdbc.queryForObject(GET_FILM_BY_ID_QUERY, mapper, id);
            if (film != null) {
                loadGenresForFilm(film);
                loadDirectorsForFilm(film);
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
        updateFilmDirector(film);
        return film;
    }

    @Override
    @Transactional
    public Film updateFilm(Film film) {
        checkFilm(film.getId());
        jdbc.update(UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateFilmGenres(film);
        updateFilmDirector(film);
        return film;
    }


    public boolean deleteFilm(long filmId) {
        checkFilm(filmId);
        int deleteCount = jdbc.update(DELETE_FILM_QUERY, filmId);
        return deleteCount > 0;
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

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        int limit = (count != null && count > 0) ? count : 10;

        if (year != null) {
            if (year < 1985 || year > LocalDate.now().getYear()) {
                throw new IllegalArgumentException("Year must be between 1985 and 1985.");
            }
        }

        if (genreId != null) {
            if (genreId < 1 || genreId > 6) {
                throw new IllegalArgumentException("Genre id must be between 1 and 6.");
            }
        }


        if (genreId != null && year != null) {
            log.debug("Executing popular films query with genreId: {}, year: {}, limit: {}", genreId, year, limit);
            List<Film> films = jdbc.query(GET_POPULAR_FILMS_BY_GENRE_AND_YEAR_QUERY, mapper, genreId, year, limit);
            loadGenresForFilms(films);
            return films;
        } else if (genreId != null && year == null) {
            log.debug("Executing popular films query with genreId: {}, limit: {}", genreId, limit);
            List<Film> films = jdbc.query(GET_POPULAR_FILMS_BY_GENRE_QUERY, mapper, genreId, limit);
            loadGenresForFilms(films);
            return films;
        } else if (year != null && genreId == null) {
            log.debug("Executing popular films query with year: {}, limit: {}", year, limit);
            List<Film> films = jdbc.query(GET_POPULAR_FILMS_BY_YEAR_QUERY, mapper, year, limit);
            loadGenresForFilms(films);
            return films;
        } else {
            log.debug("Executing popular films query with limit: {}", limit);
            List<Film> films = jdbc.query(GET_POPULAR_FILMS_QUERY, mapper, limit);
            loadGenresForFilms(films);
            loadDirectorsForFilms(films);
            return films;
        }
    }
    // endregion

    // region Like Operations
    public void addLike(long filmId, long userId) {
        checkFilm(filmId);
        checker.userExist(userId);

        jdbc.update(INSERT_FILM_LIKE_QUERY, filmId, userId);
        log.info("Like added - filmId: {}, userId: {}", filmId, userId);

    }

    @Transactional
    public void removeLike(long filmId, long userId) {
        checkFilm(filmId);
        checker.userExist(userId);

        int rowsDeleted = jdbc.update(DELETE_FILM_LIKE_QUERY, filmId, userId);
        log.debug("Deleted {} rows for filmId {} and userId {}", rowsDeleted, filmId, userId);
    }
    // endregion

    // region Genre Operations
    private void updateFilmGenres(Film film) {
        checkFilm(film.getId());

        jdbc.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> batchArgs = film.getGenres().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(Comparator.comparing(Genre::getId))
                    .map(genre -> new Object[]{film.getId(), genre.getId()})
                    .collect(Collectors.toList());

            if (!batchArgs.isEmpty()) {
                jdbc.batchUpdate("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", batchArgs);
            }
        }

        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        } else {
            Set<Genre> updatedGenres = jdbc.query(
                    "SELECT g.genre_id, g.name FROM film_genre fg " +
                            "JOIN genre g ON fg.genre_id = g.genre_id " +
                            "WHERE fg.film_id = ? ORDER BY g.genre_id",
                    (rs, rowNum) -> new Genre(rs.getLong("genre_id"), rs.getString("name")),
                    film.getId()
            ).stream().collect(Collectors.toCollection(LinkedHashSet::new));

            film.setGenres(updatedGenres.isEmpty() ? null : updatedGenres);
        }
    }

    private void updateFilmDirector(Film film) {
        checkFilm(film.getId());
        jdbc.update(DELETE_FILM_DIRECTOR_QUERY, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            List<Object[]> batchArgs = film.getDirectors().stream()
                    .map(director -> new Object[]{film.getId(), director.getId()})
                    .collect(Collectors.toList());

            jdbc.batchUpdate(INSERT_FILM_DIRECTOR_QUERY, batchArgs);
        }
    }

    private void loadGenresForFilm(Film film) {
        List<Genre> genres = jdbc.query(GET_FILM_GENRES_QUERY, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());

        // Убедимся, что жанры отсортированы по ID
        Set<Genre> sortedGenres = genres.stream()
                .sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        film.setGenres(sortedGenres);
    }

    private void loadGenresForFilms(Collection<Film> films) {
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

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
                if (film.getGenres() == null) {
                    film.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
                }
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("name"));
                film.getGenres().add(genre);
            }
        }, filmMap.keySet().toArray());

        // Гарантируем, что у всех фильмов есть Set (даже пустой)
        films.forEach(film -> {
            if (film.getGenres() == null) {
                film.setGenres(new TreeSet<>(Comparator.comparing(Genre::getId)));
            }
        });
    }
    // endregion


    //Director ops region
    private void loadDirectorsForFilm(Film film) {
        List<Director> directors = jdbc.query(GET_FILM_DIRECTORS_QUERY, (rs, rowNum) -> {
            return new Director(rs.getLong("id"), rs.getString("name"));
        }, film.getId());
        film.setDirectors(new HashSet<>(directors));
    }

    private void loadDirectorsForFilms(Collection<Film> films) {
        Map<Long, Film> filmMap = new HashMap<>();
        films.forEach(film -> filmMap.put(film.getId(), film));

        if (filmMap.isEmpty()) {
            return;
        }

        String inClause = String.join(",", Collections.nCopies(filmMap.size(), "?"));
        String query = "SELECT fd.film_id, d.id, d.name FROM film_directors fd " +
                "JOIN directors d ON fd.id = d.id " +
                "WHERE fd.film_id IN (" + inClause + ") ORDER BY d.id";

        jdbc.query(query, rs -> {
            long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                film.getDirectors().add(new Director(rs.getLong("id"), rs.getString("name")));
            }
        }, filmMap.keySet().toArray());
    }

    public List<Film> getFilmsByDirector(long directorId, String sortBy) {
        checker.directorExists(directorId);
        List<Film> films = jdbc.query(GET_FILMS_BY_DIRECTOR_QUERY.formatted(sortBy), (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));
            return film;
        }, directorId);
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        return films;
    }
    //endregion

    //Recommendations ops region

    //реализация Slope One алгоритма - ресурс (https://www.baeldung.com/java-collaborative-filtering-recommendations).
    public List<Film> getRecommendedFilms(long userId) {
        checker.userExist(userId);
        // Получаем все лайки пользователей
        List<Map<String, Object>> allLikes = jdbc.queryForList(GET_ALL_USER_LIKES);

        // Получаем фильмы, которые лайкнул текущий пользователь
        List<Long> userLikedFilms = jdbc.queryForList(
                GET_USER_LIKED_FILMS, Long.class, userId);

        // Строим матрицу пользователь-фильм
        Map<Long, Map<Long, Integer>> userItemMatrix = buildUserItemMatrix(allLikes);

        // Вычисляем средние разницы между фильмами
        Map<Long, Map<Long, Double>> deviations = computeDeviations(userItemMatrix);

        // Получаем предсказанные оценки для непросмотренных фильмов
        Map<Long, Double> predictions = predictRatings(
                userId, userLikedFilms, userItemMatrix, deviations);

        // Сортируем фильмы по предсказанным оценкам
        List<Long> recommendedFilmIds = predictions.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Получаем полную информацию о фильмах
        return getFilmsByIds(recommendedFilmIds);
    }

// Вспомогательные методы:

    private Map<Long, Map<Long, Integer>> buildUserItemMatrix(List<Map<String, Object>> likes) {
        Map<Long, Map<Long, Integer>> matrix = new HashMap<>();

        for (Map<String, Object> like : likes) {
            Long userId = ((Number) like.get("user_id")).longValue();
            Long filmId = ((Number) like.get("film_id")).longValue();

            matrix.computeIfAbsent(userId, k -> new HashMap<>())
                    .put(filmId, 1); // 1 означает лайк
        }

        return matrix;
    }

    private Map<Long, Map<Long, Double>> computeDeviations(
            Map<Long, Map<Long, Integer>> userItemMatrix) {
        Map<Long, Map<Long, List<Integer>>> freq = new HashMap<>();
        Map<Long, Map<Long, Double>> dev = new HashMap<>();

        // Для каждого пользователя
        for (Map<Long, Integer> userRatings : userItemMatrix.values()) {
            // Для каждой пары фильмов, которые оценил пользователь
            for (Map.Entry<Long, Integer> entry1 : userRatings.entrySet()) {
                Long filmId1 = entry1.getKey();
                int rating1 = entry1.getValue();

                for (Map.Entry<Long, Integer> entry2 : userRatings.entrySet()) {
                    Long filmId2 = entry2.getKey();
                    int rating2 = entry2.getValue();

                    if (!filmId1.equals(filmId2)) {
                        // Инициализируем структуры данных
                        dev.computeIfAbsent(filmId1, k -> new HashMap<>())
                                .computeIfAbsent(filmId2, k -> 0.0);

                        freq.computeIfAbsent(filmId1, k -> new HashMap<>())
                                .computeIfAbsent(filmId2, k -> new ArrayList<>());

                        // Добавляем разницу в рейтингах
                        dev.get(filmId1).put(filmId2,
                                dev.get(filmId1).get(filmId2) + (rating1 - rating2));

                        freq.get(filmId1).get(filmId2).add(1);
                    }
                }
            }
        }

        // Вычисляем средние разницы
        for (Long filmId1 : dev.keySet()) {
            for (Long filmId2 : dev.get(filmId1).keySet()) {
                int count = freq.get(filmId1).get(filmId2).size();
                dev.get(filmId1).put(filmId2,
                        dev.get(filmId1).get(filmId2) / count);
            }
        }

        return dev;
    }

    private Map<Long, Double> predictRatings(long userId,
                                             List<Long> userLikedFilms,
                                             Map<Long, Map<Long, Integer>> userItemMatrix,
                                             Map<Long, Map<Long, Double>> deviations) {
        Map<Long, Double> predictions = new HashMap<>();
        Map<Long, Integer> frequencies = new HashMap<>();

        // Для каждого фильма, который пользователь не оценил
        Set<Long> allFilmIds = getAllFilmIds();
        allFilmIds.removeAll(userLikedFilms);

        for (Long filmId : allFilmIds) {
            double sum = 0;
            int count = 0;

            // Используем оценки пользователя и отклонения для предсказания
            for (Long likedFilmId : userLikedFilms) {
                if (deviations.containsKey(likedFilmId) &&
                        deviations.get(likedFilmId).containsKey(filmId)) {

                    double deviation = deviations.get(likedFilmId).get(filmId);
                    sum += (userItemMatrix.get(userId).get(likedFilmId) + deviation);
                    count++;
                }
            }

            if (count > 0) {
                predictions.put(filmId, sum / count);
                frequencies.put(filmId, count);
            }
        }

        return predictions;
    }

    private Set<Long> getAllFilmIds() {
        return new HashSet<>(jdbc.queryForList("SELECT film_id FROM films", Long.class));
    }

    private List<Film> getFilmsByIds(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String query = GET_ALL_FILMS_QUERY + " WHERE f.film_id IN (" + inClause + ")";

        List<Film> films = jdbc.query(query, mapper, filmIds.toArray());
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);

        return films;
    }
//endregion


    // Поиск

    public List<Film> searchFilms(String query, String by) {
        log.debug("Searching films with query: '{}', by: '{}'", query, by);

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String searchPattern = "%" + query.trim() + "%";
        List<Film> films;

        if (by == null || by.trim().isEmpty()) {
            // Если параметр by не указан, ищем по названию по умолчанию
            films = jdbc.query(SEARCH_FILMS_BY_TITLE_QUERY, mapper, searchPattern);
        } else {
            // Нормализуем параметр by - убираем пробелы и приводим к нижнему регистру
            String normalizedBy = by.toLowerCase().replaceAll("\\s", "");

            if (normalizedBy.contains("title") && normalizedBy.contains("director")) {
                films = jdbc.query(SEARCH_FILMS_BY_TITLE_AND_DIRECTOR_QUERY, mapper, searchPattern, searchPattern);
            } else if (normalizedBy.contains("title")) {
                films = jdbc.query(SEARCH_FILMS_BY_TITLE_QUERY, mapper, searchPattern);
            } else if (normalizedBy.contains("director")) {
                films = jdbc.query(SEARCH_FILMS_BY_DIRECTOR_QUERY, mapper, searchPattern);
            } else {
                films = jdbc.query(SEARCH_FILMS_BY_TITLE_QUERY, mapper, searchPattern);
            }
        }

        loadGenresForFilms(films);
        loadDirectorsForFilms(films);

        log.debug("Found {} films for search query: '{}'", films.size(), query);
        return films;
    }

    //CommonFilms region
    public List<Film> getCommonFilms(long userId, long friendId) {
        List<Film> films = jdbc.query(GET_COMMON_FILMS_QUERY, mapper, userId, friendId);
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        return films;
    }
    //endregion

    //check reginon

    private void checkFilm(long filmId) {
        log.debug("Checking film with id: {}", filmId);
        if (!checker.filmExists(filmId)) {
            log.error("Film not found with id: {}", filmId);
            throw new NotFoundException("Film with id: " + filmId + " not found");
        }
    }

    public boolean mpaExists(int mpaId) {
        Integer count = jdbc.queryForObject(CHECK_MPA_EXISTS_QUERY, Integer.class, mpaId);
        return count != null && count > 0;
    }


}