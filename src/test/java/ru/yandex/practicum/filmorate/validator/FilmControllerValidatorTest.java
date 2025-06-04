package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerValidatorTest {
    private final FilmController filmController;
    private Film validFilm;

    public FilmControllerValidatorTest() {
        this.filmController = new FilmController();
    }

    @BeforeEach
    void setUp() {
        this.validFilm = new Film(
                null,
                "Valid name",
                "Valid Film Description",
                LocalDate.of(2000, 10, 1),
                125);
    }

    @Test
    void addValidFilm() {
        Film addFilm = filmController.addFilm(validFilm);

        assertNotNull(addFilm, "Film is null");
        assertEquals(1, addFilm.getId(), "Film id is incorrect");
        assertEquals("Valid Film Description", addFilm.getDescription(), "Film description is incorrect");
        assertEquals("Valid name", addFilm.getName(), "Film name is incorrect");
        assertEquals(LocalDate.of(2000, 10, 1), addFilm.getReleaseDate(), "Film release date is incorrect");
        assertEquals(125, addFilm.getDuration(), "Film duration is incorrect");
    }

    @Test
    void addNullFilm() {
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.addFilm(null));
        assertEquals("Film is null", exception.getMessage());
    }

    @Test
    void addFilmWithTooLongDescription() {
        Film addFilm = new Film(
                null,
                "name",
                String.valueOf('a').repeat(201),
                LocalDate.of(2000, 10, 1),
                125
        );

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.addFilm(addFilm));
        assertEquals("Film description cannot be longer than 200 characters", exception.getMessage());
    }

    @Test
    void theFilmShellHaveTheName() {
        Film addFilm = new Film(
                null,
                null,
                "Valid Film Description",
                LocalDate.of(2000, 10, 1),
                125
        );

        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.addFilm(addFilm));
        assertEquals("Film name cannot be empty", exception.getMessage());
    }

    @Test
    void theTheFilmReleaseDateShellNotBeBeforeExistingDate() {
        Film addFilm = new Film(
                null,
                "Valid name",
                "Valid Film Description",
                LocalDate.of(1800, 10, 1),
                125
        );
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.addFilm(addFilm));
        assertEquals("Film release date cannot be before existing(28.12.1895)", exception.getMessage());
    }

    @Test
    void theFilmShellHavePositiveAndNotZeroDuration() {
        Film addFilm1 = new Film(
                null,
                "Valid name",
                "Valid Film Description",
                LocalDate.of(2000, 10, 1),
                -1
        );

        Film addFilm2 = new Film(
                null,
                "Valid name 2",
                "Valid Film Description 2",
                LocalDate.of(2000, 10, 2),
                0
        );
        ValidationException exception1 = assertThrows(ValidationException.class, () -> filmController.addFilm(addFilm1));
        assertEquals("Film duration cannot be negative or zero", exception1.getMessage());

        ValidationException exception2 = assertThrows(ValidationException.class, () -> filmController.addFilm(addFilm2));
        assertEquals("Film duration cannot be negative or zero", exception2.getMessage());
    }

    @Test
    void updateFilmSuccessfully() {
        Film addedFilm = filmController.addFilm(validFilm);

        Film updateFilm = new Film(
                addedFilm.getId(),
                "Updated Name",
                "Updated Description",
                LocalDate.of(2001, 11, 2),
                150
        );

        Film resultFilm = filmController.updateFilm(updateFilm);

        assertEquals(addedFilm.getId(), resultFilm.getId());
        assertEquals("Updated Name", resultFilm.getName());
        assertEquals("Updated Description", resultFilm.getDescription());
        assertEquals(LocalDate.of(2001, 11, 2), resultFilm.getReleaseDate());
        assertEquals(150, resultFilm.getDuration());
    }


    @Test
    void updateFilmWithInvalidId() {
        Film invalidFilm = new Film(
                999,
                "name",
                "description",
                LocalDate.of(2000, 10, 1),
                125
        );
        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmController.updateFilm(invalidFilm));
        assertEquals("Film with id " + invalidFilm.getId() + " not found", exception.getMessage());
    }

    @Test
    void updateFilmWithNullId() {
        Film invalidFilm = new Film(
                null,
                "name",
                "description",
                LocalDate.of(2000, 10, 1),
                125
        );
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.updateFilm(invalidFilm));
        assertEquals("Film id is null", exception.getMessage());
    }

    @Test
    void updateFilmWithNullFilm() {
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.updateFilm(null));
        assertEquals("Film is null", exception.getMessage());
    }

    @Test
    void getAllFilms() {
        filmController.addFilm(validFilm);
        Collection<Film> films = filmController.getFilms();

        assertFalse(films.isEmpty());
        assertEquals(1, films.size());
        assertEquals("Valid name", films.iterator().next().getName());
    }

}