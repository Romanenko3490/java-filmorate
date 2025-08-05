package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
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

        // Обработка жанров
        if (request.getGenres() != null) {
            film.setGenres(new LinkedHashSet<>(request.getGenres()));
        } else {
            film.setGenres(new LinkedHashSet<>());
        }

        log.info("Mapping film, mpa: {}", request.getMpa());
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto filmDto = new FilmDto();
        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        filmDto.setDescription(film.getDescription());
        filmDto.setDuration(film.getDuration());
        filmDto.setReleaseDate(film.getReleaseDate());

        // Обработка жанров с проверкой на null
        if (film.getGenres() != null) {
            // Для пустого списка создаем пустой LinkedHashSet
            Set<GenreDto> genreDtos = film.getGenres().stream()
                    .filter(Objects::nonNull)
                    .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            filmDto.setGenresFromDto(genreDtos);
        } else {
            filmDto.setGenresFromDto(null);
        }

        // Обработка MPA
        if (film.getMpa() != null) {
            filmDto.setMpa(new MpaRating(film.getMpa().getId(), film.getMpa().getName(), film.getMpa().getDescription()));
        } else {
            filmDto.setMpa(new  MpaRating());
        }

        // Обработка режиссеров
        if (film.getDirectors() != null) {
            filmDto.setDirectors(new LinkedHashSet<>(film.getDirectors()));
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
            // Обновляем жанры только если они переданы в запросе
            film.setGenres(new LinkedHashSet<>(request.getGenres()));
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