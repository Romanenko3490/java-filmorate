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
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepositoryImpl;
import ru.yandex.practicum.filmorate.dal.MpaRepositoryImpl;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Slf4j
@Import({FilmDbService.class,
        FilmRepository.class,
        UserRepository.class,
        GenreRepositoryImpl.class,
        MpaRepositoryImpl.class,
        FilmRowMapper.class,
        UserRowMapper.class})
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmDbServiceTest {

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

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        newFilmRequest.setMpa(mpa);

        updateFilmRequest = new UpdateFilmRequest();
        updateFilmRequest.setId(1L);
        updateFilmRequest.setName("Updated Film");
        updateFilmRequest.setDescription("Updated Description");
        updateFilmRequest.setReleaseDate(LocalDate.of(2001, 1, 1));
        updateFilmRequest.setDuration(150);
        updateFilmRequest.setMpa(mpa);
    }

    @Test
    void shouldAddAndGetFilm() {
        FilmDto addedFilm = filmDbService.addFilm(newFilmRequest);

        FilmDto retrievedFilm = filmDbService.getFilmById(addedFilm.getId());

        assertThat(retrievedFilm.getId()).isEqualTo(addedFilm.getId());
        assertThat(retrievedFilm.getName()).isEqualTo("Test Film");
        assertThat(retrievedFilm.getDescription()).isEqualTo("Test Description");
        assertThat(retrievedFilm.getDuration()).isEqualTo(120);
        assertThat(retrievedFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    void shouldUpdateFilm() {
        FilmDto addedFilm = filmDbService.addFilm(newFilmRequest);
        updateFilmRequest.setId(addedFilm.getId());

        FilmDto updatedFilm = filmDbService.updateFilm(addedFilm.getId(), updateFilmRequest);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedFilm.getDuration()).isEqualTo(150);
    }

    @Test
    void shouldGetAllFilms() {
        filmDbService.addFilm(newFilmRequest);

        NewFilmRequest anotherFilm = new NewFilmRequest();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        MpaRating mpa = new MpaRating();
        mpa.setId(2);
        anotherFilm.setMpa(mpa);
        filmDbService.addFilm(anotherFilm);

        List<FilmDto> films = (List<FilmDto>) filmDbService.getAllFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    @Transactional
    void shouldAddAndRemoveLike() {
        // 1. Создаём фильм
        FilmDto film = filmDbService.addFilm(newFilmRequest);

        // 2. Добавляем лайк
        filmDbService.addLike(film.getId(), 1L);
        assertThat(countLikes(film.getId(), 1L)).isEqualTo(1);

        // 3. Удаляем лайк
        int deleted = jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                film.getId(), 1L
        );
        log.info("Deleted {} rows", deleted);

        // 4. Проверяем
        assertThat(countLikes(film.getId(), 1L))
                .as("Лайк должен быть удалён из БД")
                .isEqualTo(0);
    }

    private int countLikes(long filmId, long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                filmId,
                userId
        );
        return count != null ? count : 0;
    }

    @Test
    void shouldGetPopularFilms() {
        FilmDto film1 = filmDbService.addFilm(newFilmRequest);

        NewFilmRequest anotherFilm = new NewFilmRequest();
        anotherFilm.setName("More Popular Film");
        anotherFilm.setDescription("More Popular Description");
        anotherFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        anotherFilm.setDuration(90);
        MpaRating mpa = new MpaRating();
        mpa.setId(2);
        anotherFilm.setMpa(mpa);
        FilmDto film2 = filmDbService.addFilm(anotherFilm);

        // Добавляем лайки - film2 должен быть популярнее
        filmDbService.addLike(film1.getId(), 1L);
        filmDbService.addLike(film2.getId(), 1L);
        filmDbService.addLike(film2.getId(), 2L);

        List<FilmDto> popularFilms = filmDbService.getMostPopularFilms(2);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(film2.getId());
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        assertThrows(NotFoundException.class, () -> filmDbService.getFilmById(999L));
    }

    @Test
    void shouldThrowWhenMpaNotFound() {
        newFilmRequest.getMpa().setId(999);
        assertThrows(NotFoundException.class, () -> filmDbService.addFilm(newFilmRequest));
    }
}