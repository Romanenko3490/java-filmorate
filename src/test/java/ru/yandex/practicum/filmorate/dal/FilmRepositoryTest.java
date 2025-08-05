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
import ru.yandex.practicum.filmorate.service.EntityCheckService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FilmRepository.class,
        GenreRepositoryImpl.class,
        MpaRepositoryImpl.class,
        EntityChecker.class,
        EntityCheckService.class,
        FilmRowMapper.class})
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

        Set<MpaRating> mpaRatings = new HashSet<>();
        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        mpaRatings.add(mpa);
        testFilm.setMpa(mpaRatings);

        Set<Genre> genres = new HashSet<>();
        Genre genre = new Genre();
        genre.setId(1L);
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
                    assertThat(film.getMpa())
                            .hasSize(1)
                            .extracting(MpaRating::getId)
                            .containsExactly(1L);
                    assertThat(film.getGenres())
                            .hasSize(1)
                            .extracting(Genre::getId)
                            .containsExactly(1L);
                });
    }

    @Test
    void shouldUpdateFilm() {
        Film addedFilm = filmRepository.addFilm(testFilm);

        addedFilm.setName("Updated Name");
        addedFilm.setDescription("Updated Description");
        addedFilm.setDuration(150);

        // Добавим еще один MPA рейтинг
        MpaRating newMpa = new MpaRating();
        newMpa.setId(2L);
        addedFilm.getMpa().add(newMpa);

        // Добавим еще один жанр
        Genre newGenre = new Genre();
        newGenre.setId(2L);
        addedFilm.getGenres().add(newGenre);

        Film updatedFilm = filmRepository.updateFilm(addedFilm);

        Optional<Film> retrievedFilm = filmRepository.getFilmById(addedFilm.getId());

        assertThat(retrievedFilm)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName()).isEqualTo("Updated Name");
                    assertThat(film.getDescription()).isEqualTo("Updated Description");
                    assertThat(film.getDuration()).isEqualTo(150);
                    assertThat(film.getMpa())
                            .hasSize(2)
                            .extracting(MpaRating::getId)
                            .containsExactlyInAnyOrder(1L, 2L);
                    assertThat(film.getGenres())
                            .hasSize(2)
                            .extracting(Genre::getId)
                            .containsExactlyInAnyOrder(1L, 2L);
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

        Set<MpaRating> mpaRatings = new HashSet<>();
        MpaRating mpa = new MpaRating();
        mpa.setId(2L);
        mpaRatings.add(mpa);
        anotherFilm.setMpa(mpaRatings);

        filmRepository.addFilm(anotherFilm);

        Collection<Film> films = filmRepository.getFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film addedFilm = filmRepository.addFilm(testFilm);

        filmRepository.addLike(addedFilm.getId(), 1L);

        List<Film> popularFilms = filmRepository.getPopularFilms(10, null, null);

        assertThat(popularFilms).extracting(Film::getId).contains(addedFilm.getId());
        assertThat(popularFilms.get(0).getId()).isEqualTo(addedFilm.getId());

        filmRepository.removeLike(addedFilm.getId(), 1L);

        List<Film> filmsAfterRemove = filmRepository.getPopularFilms(10, null, null);

        if (filmsAfterRemove.size() > 1) {
            assertThat(filmsAfterRemove.stream().limit(1))
                    .extracting(Film::getId)
                    .doesNotContain(addedFilm.getId());
        } else {
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

        Set<MpaRating> mpaRatings = new HashSet<>();
        MpaRating mpa = new MpaRating();
        mpa.setId(2L);
        mpaRatings.add(mpa);
        film2.setMpa(mpaRatings);

        film2 = filmRepository.addFilm(film2);

        filmRepository.addLike(film1.getId(), 1L);
        filmRepository.addLike(film2.getId(), 1L);
        filmRepository.addLike(film2.getId(), 2L);

        List<Film> popularFilms = filmRepository.getPopularFilms(2, null, null);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film2.getId());
    }

    @Test
    void shouldGetPopularFilmsByGenre() {
        Film film1 = filmRepository.addFilm(testFilm);

        Film film2 = new Film();
        film2.setName("Film with Different Genre");
        film2.setDescription("Description");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(90);
        film2.setLikes(new HashSet<>());

        Set<MpaRating> mpaRatings = new HashSet<>();
        MpaRating mpa = new MpaRating();
        mpa.setId(2L);
        mpaRatings.add(mpa);
        film2.setMpa(mpaRatings);

        Set<Genre> genres = new HashSet<>();
        Genre genre = new Genre();
        genre.setId(2L);
        genres.add(genre);
        film2.setGenres(genres);

        filmRepository.addFilm(film2);

        filmRepository.addLike(film1.getId(), 1L);
        filmRepository.addLike(film2.getId(), 1L);

        List<Film> popularFilms = filmRepository.getPopularFilms(10, 1, null);

        assertThat(popularFilms)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(film1.getId());
    }

    @Test
    void shouldGetPopularFilmsByYear() {
        Film film1 = filmRepository.addFilm(testFilm);

        Film film2 = new Film();
        film2.setName("Film from Different Year");
        film2.setDescription("Description");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(90);
        film2.setLikes(new HashSet<>());

        Set<MpaRating> mpaRatings = new HashSet<>();
        MpaRating mpa = new MpaRating();
        mpa.setId(2L);
        mpaRatings.add(mpa);
        film2.setMpa(mpaRatings);

        filmRepository.addFilm(film2);

        filmRepository.addLike(film1.getId(), 1L);
        filmRepository.addLike(film2.getId(), 1L);

        List<Film> popularFilms = filmRepository.getPopularFilms(10, null, 2000);

        assertThat(popularFilms)
                .hasSize(1)
                .extracting(Film::getId)
                .containsExactly(film1.getId());
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> {
            Film film = new Film();
            film.setId(999L);
            film.setName("Test Film");
            film.setDescription("Test Description");
            film.setReleaseDate(LocalDate.now());
            film.setDuration(120);

            Set<MpaRating> mpaRatings = new HashSet<>();
            MpaRating mpa = new MpaRating();
            mpa.setId(1L);
            mpaRatings.add(mpa);
            film.setMpa(mpaRatings);

            filmRepository.updateFilm(film);
        });
    }

    @Test
    void shouldHandleMultipleMpaRatings() {
        // Добавляем фильм с несколькими MPA рейтингами
        Set<MpaRating> mpaRatings = new HashSet<>();
        MpaRating mpa1 = new MpaRating();
        mpa1.setId(1L);
        MpaRating mpa2 = new MpaRating();
        mpa2.setId(2L);
        mpaRatings.add(mpa1);
        mpaRatings.add(mpa2);
        testFilm.setMpa(mpaRatings);

        Film addedFilm = filmRepository.addFilm(testFilm);

        Optional<Film> retrievedFilm = filmRepository.getFilmById(addedFilm.getId());

        assertThat(retrievedFilm)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getMpa())
                            .hasSize(2)
                            .extracting(MpaRating::getId)
                            .containsExactlyInAnyOrder(1L, 2L);
                });
    }
}