package ru.yandex.practicum.filmorate.dto;

import lombok.Data;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
public class FilmDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Map<Long, String> genres;
    private MpaDto mpa;
    private Set<Director> directors;

    public FilmDto() {
        this.genres = new TreeMap<>(); // Инициализация TreeMap для автоматической сортировки
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
        this.setGenres(genres); // Используем сеттер для установки жанров
        this.setMpa(mpa);
        this.directors = directors;
    }

    public void setGenres(Set<Genre> genres) {
        if (genres == null) {
            this.genres = new TreeMap<>();
            return;
        }

        this.genres = genres.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Genre::getId,
                        g -> g.getName() != null ? g.getName() : "",
                        (existing, replacement) -> existing,
                        TreeMap::new
                ));
    }

    public void setMpa(MpaRating mpa) {
        if (mpa != null) {
            this.mpa = new MpaDto(mpa.getId(), mpa.getName());
        }
    }
}