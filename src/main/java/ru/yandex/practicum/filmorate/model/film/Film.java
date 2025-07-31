package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;

    @NotBlank(message = "Film name cannot be blank or empty")
    private String name;

    @Size(max = 200, message = "Film description cannot be longer than 200 characters")
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @NotNull(message = "Please complete field \"duration\"")
    @Positive(message = "Film duration cannot be negative or zero")
    private Integer duration;

    private Set<Genre> genres = new HashSet<>();
    private MpaRating mpa;

    private Set<Long> likes = new HashSet<>();
    private Set<Director> director = new HashSet<>();


    public Film(Long id, String name, String description, LocalDate releaseDate, Integer duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public Film() {
    }

    @AssertTrue(message = "Release date must be on or after 1895-12-28")
    public boolean hasReleaseDate() {
        return !(releaseDate == null || releaseDate.isBefore(LocalDate.of(1895, 12, 28)));
    }

    public Integer getLikesCount() {
        return likes.size();
    }

}
