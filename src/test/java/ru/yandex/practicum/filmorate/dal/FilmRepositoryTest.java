package ru.yandex.practicum.filmorate.dal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmRepository.class, GenreRepositoryImpl.class, MpaRepositoryImpl.class, FilmRowMapper.class})
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmRepositoryTest {

    @Autowired
    private FilmRepository filmRepository;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setLikes(new HashSet<>());

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        testFilm.setMpa(mpa);

        Set<Genre> genres = new HashSet<>();
        Genre genre = new Genre();
        genre.setId(1);
        genres.add(genre);
        testFilm.setGenres(genres);
    }

    @Test
    void shouldAddAndGetFilm() {
        Film addedFilm = filmRepository.addFilm(testFilm);

        Optional<Film> retrievedFilm = filmRepository.getFilmById(addedFilm.getId());

        assertThat(retrievedFilm)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(addedFilm.getId());
                    assertThat(film.getName()).isEqualTo("Test Film");
                    assertThat(film.getDescription()).isEqualTo("Test Description");
                    assertThat(film.getDuration()).isEqualTo(120);
                    assertThat(film.getMpa().getId()).isEqualTo(1);
                    assertThat(film.getGenres()).hasSize(1);
                });
    }

    @Test
    void shouldUpdateFilm() {
        Film addedFilm = filmRepository.addFilm(testFilm);

        addedFilm.setName("Updated Name");
        addedFilm.setDescription("Updated Description");
        addedFilm.setDuration(150);

        Film updatedFilm = filmRepository.updateFilm(addedFilm);

        Optional<Film> retrievedFilm = filmRepository.getFilmById(addedFilm.getId());

        assertThat(retrievedFilm)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName()).isEqualTo("Updated Name");
                    assertThat(film.getDescription()).isEqualTo("Updated Description");
                    assertThat(film.getDuration()).isEqualTo(150);
                });
    }

    @Test
    void shouldGetAllFilms() {
        filmRepository.addFilm(testFilm);

        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        anotherFilm.setLikes(new HashSet<>());
        MpaRating mpa = new MpaRating();
        mpa.setId(2);
        anotherFilm.setMpa(mpa);
        filmRepository.addFilm(anotherFilm);

        Collection<Film> films = filmRepository.getFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film addedFilm = filmRepository.addFilm(testFilm);

        filmRepository.addLike(addedFilm.getId(), 1L);

        List<Film> popularFilms = filmRepository.getPopularFilms(10);
        assertThat(popularFilms).extracting(Film::getId).contains(addedFilm.getId());
        assertThat(popularFilms.get(0).getId()).isEqualTo(addedFilm.getId()); // Проверяем, что он на первом месте

        filmRepository.removeLike(addedFilm.getId(), 1L);

        List<Film> filmsAfterRemove = filmRepository.getPopularFilms(10);

        if (filmsAfterRemove.size() > 1) {
            assertThat(filmsAfterRemove.stream().limit(1))
                    .extracting(Film::getId)
                    .doesNotContain(addedFilm.getId());
        } else {
            // Если других фильмов нет, проверяем, что у него 0 лайков
            assertThat(filmsAfterRemove.get(0).getLikes()).isEmpty();
        }
    }

    @Test
    void shouldGetPopularFilms() {
        Film film1 = filmRepository.addFilm(testFilm);
        Film film2 = new Film();
        film2.setName("More Popular Film");
        film2.setDescription("More Popular Description");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(90);
        film2.setLikes(new HashSet<>());
        MpaRating mpa = new MpaRating();
        mpa.setId(2);
        film2.setMpa(mpa);
        film2 = filmRepository.addFilm(film2);

        filmRepository.addLike(film1.getId(), 1L);
        filmRepository.addLike(film2.getId(), 1L);
        filmRepository.addLike(film2.getId(), 2L);

        List<Film> popularFilms = filmRepository.getPopularFilms(2);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film2.getId());
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> {
            Film film = new Film();
            film.setId(999L);
            film.setLikes(new HashSet<>());
            filmRepository.updateFilm(film);
        });
    }
}