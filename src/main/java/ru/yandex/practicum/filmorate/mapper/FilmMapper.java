package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {
    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setDuration(request.getDuration());
        film.setGenres(request.getGenres());
        film.setReleaseDate(request.getReleaseDate());
        film.setMpa(request.getMpa());
        film.setDirectors(request.getDirectors());
        log.info("Mapping film, mpa: {}", request.getMpa());
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = new FilmDto();
        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        filmDto.setDescription(film.getDescription());
        filmDto.setDuration(film.getDuration());
        if (film.getGenres() != null) {
            filmDto.setGenresIdList(film.getGenres().stream().map(Genre::getId).sorted().collect(Collectors.toList()));
        }
        filmDto.setReleaseDate(film.getReleaseDate());
        if (film.getMpa() != null) {
            filmDto.setMpaId(film.getMpa().getId());
        }
        if (film.getDirectors() != null) {
            filmDto.setDirectors(film.getDirectors());
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
            film.setGenres(request.getGenres());
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
