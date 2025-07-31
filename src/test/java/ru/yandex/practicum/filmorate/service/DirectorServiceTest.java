package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.DirectorRepositoryImpl;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Slf4j
@Import({
        DirectorService.class,
        DirectorRepositoryImpl.class,
        DirectorRowMapper.class
})
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class DirectorServiceTest {

    @Autowired
    private DirectorService directorService;

    private Director director1;
    private Director director2;

    @BeforeEach
    void setUp() {
        director1 = new Director();
        director1.setName("Director One");

        director2 = new Director();
        director2.setName("Director Two");
    }

    @Test
    void shouldFindDirectorById() {
        DirectorDto createdDirector = directorService.add(director1);
        DirectorDto foundDirector = directorService.findById(createdDirector.getDirectorId());

        assertThat(foundDirector.getDirectorId()).isEqualTo(createdDirector.getDirectorId());
        assertThat(foundDirector.getName()).isEqualTo("Director One");
    }

    @Test
    void shouldThrowWhenDirectorNotFound() {
        assertThrows(NotFoundException.class,
                () -> directorService.findById(999));
    }

    @Test
    void shouldFindAllDirectors() {
        directorService.add(director1);
        directorService.add(director2);

        List<DirectorDto> directors = directorService.findAll();

        assertThat(directors).hasSize(2);
        assertThat(directors.get(0).getName()).isEqualTo("Director One");
        assertThat(directors.get(1).getName()).isEqualTo("Director Two");
    }

    @Test
    void shouldAddDirector() {
        DirectorDto addedDirector = directorService.add(director1);

        assertThat(addedDirector.getDirectorId()).isPositive();
        assertThat(addedDirector.getName()).isEqualTo("Director One");

        DirectorDto foundDirector = directorService.findById(addedDirector.getDirectorId());
        assertThat(foundDirector).isEqualTo(addedDirector);
    }

    @Test
    void shouldUpdateDirector() {
        DirectorDto addedDirector = directorService.add(director1);

        Director updatedDirector = new Director();
        updatedDirector.setDirectorId(addedDirector.getDirectorId());
        updatedDirector.setName("Updated Director Name");

        DirectorDto updatedDirectorDto = directorService.update(updatedDirector);

        assertThat(updatedDirectorDto.getDirectorId()).isEqualTo(addedDirector.getDirectorId());
        assertThat(updatedDirectorDto.getName()).isEqualTo("Updated Director Name");

        DirectorDto foundDirector = directorService.findById(addedDirector.getDirectorId());
        assertThat(foundDirector.getName()).isEqualTo("Updated Director Name");
    }

    @Test
    void shouldThrowWhenUpdateNonExistentDirector() {
        Director nonExistentDirector = new Director();
        nonExistentDirector.setDirectorId(999L);
        nonExistentDirector.setName("Non-existent");

        assertThrows(NotFoundException.class,
                () -> directorService.update(nonExistentDirector));
    }

    @Test
    void shouldDeleteDirector() {
        DirectorDto addedDirector = directorService.add(director1);
        directorService.delete(addedDirector.getDirectorId());

        assertThrows(NotFoundException.class,
                () -> directorService.findById(addedDirector.getDirectorId()));
    }

    @Test
    void shouldThrowWhenDeleteNonExistentDirector() {
        assertThrows(NotFoundException.class,
                () -> directorService.delete(999));
    }

    @Test
    void shouldReturnEmptyListWhenNoDirectorsExist() {
        List<DirectorDto> directors = directorService.findAll();
        assertThat(directors).isEmpty();
    }
}