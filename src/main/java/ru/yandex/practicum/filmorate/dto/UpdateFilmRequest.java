package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    @NotNull(message = "Film ID cannot be null")
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Genre> genres;
    private MpaRating mpa;
    private Set<Director> directors;

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return !(releaseDate == null || releaseDate.isBefore(LocalDate.now()));
    }

    public boolean hasDuration() {
        return !(duration == null || duration <= 0);
    }

    public boolean hasGenres() {
        if (genres == null || genres.isEmpty()) {
            return false;
        }
        return genres.stream().allMatch(genre -> genre.getId() > 0 && genre.getId() <= 6);
    }

    public boolean hasMpaRating() {
        return mpa != null && mpa.getId() > 0 && mpa.getId() <= 5;
    }

}
