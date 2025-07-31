package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NewFilmRequest {
    @NotBlank(message = "Film name cannot be blank or empty")
    private String name;

    @Size(max = 200, message = "Film description cannot be longer than 200 characters")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @NotNull(message = "Please complete field \"duration\"")
    @Positive(message = "Film duration cannot be negative or zero")
    private Integer duration;

    private Set<Genre> genres;

    private MpaRating mpa;

    private Set<Director> director;

    @AssertTrue(message = "Release date must be on or after 1895-12-28")
    public boolean hasReleaseDate() {
        return !(releaseDate == null || releaseDate.isBefore(LocalDate.of(1895, 12, 28)));
    }
}
