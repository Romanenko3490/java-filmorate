package ru.yandex.practicum.filmorate.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class InMemoryFilmStorageTest {
    private final InMemoryFilmStorage inMemoryFilmStorage;
    private Film validFilm;

    public InMemoryFilmStorageTest() {
        this.inMemoryFilmStorage = new InMemoryFilmStorage();
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
        Film addFilm = inMemoryFilmStorage.addFilm(validFilm);

        assertNotNull(addFilm, "Film is null");
        assertEquals(1, addFilm.getId(), "Film id is incorrect");
        assertEquals("Valid Film Description", addFilm.getDescription(), "Film description is incorrect");
        assertEquals("Valid name", addFilm.getName(), "Film name is incorrect");
        assertEquals(LocalDate.of(2000, 10, 1), addFilm.getReleaseDate(), "Film release date is incorrect");
        assertEquals(125, addFilm.getDuration(), "Film duration is incorrect");
    }

    @Test
    void addNullFilm() {
        ValidationException exception = assertThrows(ValidationException.class, () -> inMemoryFilmStorage.addFilm(null));
        assertEquals("Film is null", exception.getMessage());
    }


    @Test
    void updateFilmSuccessfully() {
        Film addedFilm = inMemoryFilmStorage.addFilm(validFilm);

        Film updateFilm = new Film(
                addedFilm.getId(),
                "Updated Name",
                "Updated Description",
                LocalDate.of(2001, 11, 2),
                150
        );

        Film resultFilm = inMemoryFilmStorage.updateFilm(updateFilm);

        assertEquals(addedFilm.getId(), resultFilm.getId());
        assertEquals("Updated Name", resultFilm.getName());
        assertEquals("Updated Description", resultFilm.getDescription());
        assertEquals(LocalDate.of(2001, 11, 2), resultFilm.getReleaseDate());
        assertEquals(150, resultFilm.getDuration());
    }


    @Test
    void getAllFilms() {
        inMemoryFilmStorage.addFilm(validFilm);
        Collection<Film> films = inMemoryFilmStorage.getFilms();

        assertFalse(films.isEmpty());
        assertEquals(1, films.size());
        assertEquals("Valid name", films.iterator().next().getName());
    }

}