package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {
    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setDuration(request.getDuration());
        film.setReleaseDate(request.getReleaseDate());
        film.setMpa(request.getMpa());
        film.setDirectors(request.getDirectors());

        // Используем TreeSet для автоматической сортировки
        if (request.getGenres() != null) {
            film.setGenres(new TreeSet<>(request.getGenres()));
        } else {
            film.setGenres(new TreeSet<>());
        }

        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = new FilmDto();
        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        filmDto.setDescription(film.getDescription());
        filmDto.setDuration(film.getDuration());
        filmDto.setReleaseDate(film.getReleaseDate());

        // TreeSet автоматически сортирует благодаря Comparable в Genre
        Set<GenreDto> genreDtos = film.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .collect(Collectors.toCollection(TreeSet::new));

        filmDto.setGenresFromDto(genreDtos);

        if (film.getMpa() != null) {
            filmDto.setMpa(film.getMpa());
        }

        if (film.getDirectors() != null) {
            filmDto.setDirectors(new TreeSet<>(film.getDirectors()));
        } else {
            filmDto.setDirectors(new TreeSet<>());
        }

        return filmDto;
    }

    public static Film updateFilm(Film film, UpdateFilmRequest request) {
        if (request.hasName()) {
            film.setName(request.getName());
        }

        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }

        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }

        if (request.hasGenres()) {
            film.setGenres(new TreeSet<>(request.getGenres()));
        }

        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }

        if (request.hasMpaRating()) {
            film.setMpa(request.getMpa());
        }

        return film;
    }
}