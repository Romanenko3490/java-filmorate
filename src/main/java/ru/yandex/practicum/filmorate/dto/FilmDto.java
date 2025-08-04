package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<GenreDto> genres = new LinkedHashSet<>();
    private MpaDto mpa;
    private Set<Director> directors = new  LinkedHashSet<>();

    public FilmDto() {
    }

    public FilmDto(Long id,
                   String name,
                   String description,
                   LocalDate releaseDate,
                   Integer duration,
                   Set<Genre> genres,
                   MpaRating mpa,
                   Set<Director> directors) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.setGenres(genres);
        this.setMpa(mpa);
        this.directors = directors;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres.clear(); // Очищаем перед добавлением новых
        if (genres != null) {
            this.genres.addAll(genres.stream()
                    .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                    .collect(Collectors.toList()));
        }
    }

    public void setMpa(MpaRating mpa) {
        this.mpa = mpa != null ? new MpaDto(mpa.getId(), mpa.getName()) : null;
    }

    public void setDirectors(Set<Director> directors) {
        this.directors.clear();
        if (directors != null) {
            this.directors.addAll(directors);
        }
    }
}