package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private List<GenreDto> genres;
    private MpaDto mpa;
    private Set<Director> directors;


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
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = genres.stream()
                .map(genre -> new GenreDto(genre.getId()))
                .sorted((g1, g2) -> Long.compare(g1.getId(), g2.getId()))
                .collect(Collectors.toList());
        this.mpa = new MpaDto(mpa.getId(), mpa.getName());
        this.directors = directors;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres.stream()
                .map(genre -> new GenreDto(genre.getId()))
                .sorted((g1, g2) -> Long.compare(g1.getId(), g2.getId()))
                .collect(Collectors.toList());
    }

    public void setMpa(MpaRating mpa) {
        this.mpa = new MpaDto(mpa.getId(), mpa.getName());
    }
}
