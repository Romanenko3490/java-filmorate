package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Slf4j
@Import({
        FilmDbService.class,
        FilmRepository.class,
        UserRepository.class,
        GenreRepositoryImpl.class,
        MpaRepositoryImpl.class,
        DirectorRepositoryImpl.class,
        FilmRowMapper.class,
        UserRowMapper.class,
        UserDbService.class,
        EntityChecker.class,
        EntityCheckService.class,
        FriendshipRepository.class,
        DirectorRowMapper.class
})
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmsTest {

    @Autowired
    private FilmDbService filmDbService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private NewFilmRequest newFilmRequest;
    private UpdateFilmRequest updateFilmRequest;

    @BeforeEach
    void setUp() {
        newFilmRequest = new NewFilmRequest();
        newFilmRequest.setName("Test Film");
        newFilmRequest.setDescription("Test Description");
        newFilmRequest.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilmRequest.setDuration(120);
        Set<MpaRating> mpaRatings = new HashSet<>();
        mpaRatings.add(new MpaRating(1L, "G", "General Audiences"));
        newFilmRequest.setMpa(mpaRatings);
        newFilmRequest.setDirectors(new HashSet<>());

        updateFilmRequest = new UpdateFilmRequest();
        updateFilmRequest.setId(1L);
        updateFilmRequest.setName("Updated Film");
        updateFilmRequest.setDescription("Updated Description");
        updateFilmRequest.setReleaseDate(LocalDate.of(2001, 1, 1));
        updateFilmRequest.setDuration(150);
        Set<MpaRating> updatedMpaRatings = new HashSet<>();
        updatedMpaRatings.add(new MpaRating(1L, "G", "General Audiences"));
        updateFilmRequest.setMpa(updatedMpaRatings);
        updateFilmRequest.setDirectors(new HashSet<>());
    }

    @Test
    void shouldAddAndGetFilm() {
        FilmDto addedFilm = filmDbService.addFilm(newFilmRequest);

        FilmDto retrievedFilm = filmDbService.getFilmById(addedFilm.getId());

        assertThat(retrievedFilm)
                .isNotNull()
                .isEqualToComparingFieldByField(addedFilm);

        assertThat(retrievedFilm.getName()).isEqualTo("Test Film");
        assertThat(retrievedFilm.getDescription()).isEqualTo("Test Description");
        assertThat(retrievedFilm.getDuration()).isEqualTo(120);
        assertThat(retrievedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(retrievedFilm.getMpa()).hasSize(1);
        assertThat(retrievedFilm.getMpa().iterator().next().getId()).isEqualTo(1L);
    }

    @Test
    void shouldUpdateFilm() {
        FilmDto addedFilm = filmDbService.addFilm(newFilmRequest);
        updateFilmRequest.setId(addedFilm.getId());

        FilmDto updatedFilm = filmDbService.updateFilm(addedFilm.getId(), updateFilmRequest);

        assertThat(updatedFilm.getId()).isEqualTo(addedFilm.getId());
        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);
        assertThat(updatedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2001, 1, 1));
    }

    @Test
    void shouldGetAllFilms() {
        FilmDto film1 = filmDbService.addFilm(newFilmRequest);

        NewFilmRequest anotherFilm = new NewFilmRequest();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        Set<MpaRating> mpaRatings = new HashSet<>();
        mpaRatings.add(new MpaRating(2L, "PG", "Parental Guidance Suggested"));
        anotherFilm.setMpa(mpaRatings);
        anotherFilm.setDirectors(new HashSet<>());

        FilmDto film2 = filmDbService.addFilm(anotherFilm);

        List<FilmDto> films = (List<FilmDto>) filmDbService.getAllFilms();

        assertThat(films)
                .hasSize(2)
                .extracting(FilmDto::getId)
                .containsExactlyInAnyOrder(film1.getId(), film2.getId());
    }

    @Test
    void shouldAddAndRemoveLike() {
        FilmDto film = filmDbService.addFilm(newFilmRequest);

        filmDbService.addLike(film.getId(), 1L);
        assertThat(countFilmLikes(film.getId(), 1L)).isEqualTo(1);

        filmDbService.removeLike(film.getId(), 1L);
        assertThat(countFilmLikes(film.getId(), 1L))
                .as("Лайк должен быть удалён из БД")
                .isEqualTo(0);
    }

    @Test
    void shouldGetPopularFilms() {
        FilmDto film1 = filmDbService.addFilm(newFilmRequest);

        NewFilmRequest anotherFilm = new NewFilmRequest();
        anotherFilm.setName("More Popular Film");
        anotherFilm.setDescription("More Popular Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        Set<MpaRating> mpaRatings = new HashSet<>();
        mpaRatings.add(new MpaRating(2L, "PG", "Parental Guidance Suggested"));
        anotherFilm.setMpa(mpaRatings);
        anotherFilm.setDirectors(new HashSet<>());

        FilmDto film2 = filmDbService.addFilm(anotherFilm);

        filmDbService.addLike(film1.getId(), 1L);
        filmDbService.addLike(film2.getId(), 1L);
        filmDbService.addLike(film2.getId(), 2L);

        List<FilmDto> popularFilms = filmDbService.getMostPopularFilms(2, null, null);

        assertThat(popularFilms)
                .hasSize(2)
                .extracting(FilmDto::getId)
                .containsExactly(film2.getId(), film1.getId());
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmDbService.getFilmById(999L));
    }

    @Test
    void shouldThrowWhenMpaNotFound() {
        Set<MpaRating> invalidMpa = new HashSet<>();
        invalidMpa.add(new MpaRating(999L, null, null));
        newFilmRequest.setMpa(invalidMpa);

        assertThrows(NotFoundException.class, () -> filmDbService.addFilm(newFilmRequest));
    }

    @Test
    void shouldDeleteFilmAndCascadeRelations() {
        FilmDto film = filmDbService.addFilm(newFilmRequest);

        filmDbService.addLike(film.getId(), 1L);
        jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)",
                film.getId(), 1);

        filmDbService.removeFilm(film.getId());
        assertThrows(NotFoundException.class, () -> filmDbService.getFilmById(film.getId()));

        assertThat(countFilmLikes(film.getId()))
                .as("likes relations should be deleted")
                .isZero();

        assertThat(countFilmGenres(film.getId()))
                .as("genres relations should be deleted")
                .isZero();
    }

    @Test
    void shouldGetCommonFilms() {
        Long user1Id = 1L;
        Long user2Id = 2L;

        FilmDto film1 = filmDbService.addFilm(newFilmRequest);

        NewFilmRequest anotherFilm = new NewFilmRequest();
        anotherFilm.setName("Common Film");
        anotherFilm.setDescription("Film liked by both users");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        Set<MpaRating> mpaRatings = new HashSet<>();
        mpaRatings.add(new MpaRating(2L, "PG", "Parental Guidance Suggested"));
        anotherFilm.setMpa(mpaRatings);
        anotherFilm.setDirectors(new HashSet<>());

        FilmDto film2 = filmDbService.addFilm(anotherFilm);

        filmDbService.addLike(film1.getId(), user1Id);
        filmDbService.addLike(film2.getId(), user1Id);
        filmDbService.addLike(film2.getId(), user2Id);

        List<FilmDto> commonFilms = filmDbService.getCommonFilms(user1Id, user2Id);

        assertThat(commonFilms)
                .hasSize(1)
                .extracting(FilmDto::getId)
                .containsExactly(film2.getId());
    }

    @Test
    void shouldHandleFilmWithDirectors() {
        // Add a director first
        Director director = new Director(1L, "Test Director");
        jdbcTemplate.update("INSERT INTO directors (id, name) VALUES (?, ?)",
                director.getId(), director.getName());

        Set<Director> directors = new HashSet<>();
        directors.add(director);
        newFilmRequest.setDirectors(directors);

        FilmDto addedFilm = filmDbService.addFilm(newFilmRequest);

        assertThat(addedFilm.getDirectors())
                .hasSize(1)
                .extracting(Director::getName)
                .containsExactly("Test Director");
    }

    @Test
    void shouldSearchFilms() {
        FilmDto film1 = filmDbService.addFilm(newFilmRequest);

        // Add a film with director
        Director director = new Director(1L, "Test Director");
        jdbcTemplate.update("INSERT INTO directors (id, name) VALUES (?, ?)",
                director.getId(), director.getName());

        NewFilmRequest anotherFilm = new NewFilmRequest();
        anotherFilm.setName("Film with Director");
        anotherFilm.setDescription("Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        Set<MpaRating> mpaRatings = new HashSet<>();
        mpaRatings.add(new MpaRating(2L, "PG", "Parental Guidance Suggested"));
        anotherFilm.setMpa(mpaRatings);
        Set<Director> directors = new HashSet<>();
        directors.add(director);
        anotherFilm.setDirectors(directors);

        FilmDto film2 = filmDbService.addFilm(anotherFilm);

        // Search by title
        List<FilmDto> titleResults = filmDbService.searchFilms("Test", "title");
        assertThat(titleResults)
                .hasSize(1)
                .extracting(FilmDto::getId)
                .containsExactly(film1.getId());

        // Search by director
        List<FilmDto> directorResults = filmDbService.searchFilms("Test", "director");
        assertThat(directorResults)
                .hasSize(1)
                .extracting(FilmDto::getId)
                .containsExactly(film2.getId());

        // Search by both
        List<FilmDto> bothResults = filmDbService.searchFilms("Test", "title,director");
        assertThat(bothResults)
                .hasSize(2)
                .extracting(FilmDto::getId)
                .containsExactlyInAnyOrder(film1.getId(), film2.getId());
    }

    private int countFilmLikes(long filmId, long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                filmId,
                userId
        );
        return count != null ? count : 0;
    }

    private int countFilmLikes(long filmId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ?",
                Integer.class,
                filmId
        );
        return count != null ? count : 0;
    }

    private int countFilmGenres(long filmId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_genre WHERE film_id = ?",
                Integer.class,
                filmId
        );
        return count != null ? count : 0;
    }
}