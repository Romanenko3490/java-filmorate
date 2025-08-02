package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.enums.OrderBy;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Primary
@RequiredArgsConstructor
public class FilmDbService {
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;


    public Collection<FilmDto> getAllFilms() {
        log.info("Getting all films");
        return filmRepository.getFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilmById(long filmId) {
        log.info("Getting film by id: {}", filmId);
        return filmRepository.getFilmById(filmId)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Film with id: " + filmId));
    }

    public FilmDto addFilm(NewFilmRequest request) {
        if (!mpaRepository.existsById(request.getMpa().getId())) {
            throw new NotFoundException("MPA rating with id " + request.getMpa().getId() + " not found");
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            Set<Long> genreIds = request.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            Set<Long> existingGenreIds = genreRepository.findAllExistingIds(genreIds);

            Set<Long> missingGenreIds = genreIds.stream()
                    .filter(id -> !existingGenreIds.contains(id))
                    .collect(Collectors.toSet());

            if (!missingGenreIds.isEmpty()) {
                throw new NotFoundException("Genres with ids " + missingGenreIds + " not found");
            }
        }

        validateDirectors(request.getDirectors());

        Film film = FilmMapper.mapToFilm(request);
        filmRepository.addFilm(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto updateFilm(long filmId, UpdateFilmRequest request) {
        Film film = filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film not found with id: " + filmId));

        if (request.getName() != null) film.setName(request.getName());
        if (request.getDescription() != null) film.setDescription(request.getDescription());
        if (request.getReleaseDate() != null) film.setReleaseDate(request.getReleaseDate());
        if (request.getDuration() != null) film.setDuration(request.getDuration());
        if (request.getMpa() != null) film.setMpa(request.getMpa());
        if (request.getGenres() != null) film.setGenres(request.getGenres());
        if (request.getDirectors() != null) film.setDirectors(request.getDirectors());

        filmRepository.updateFilm(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto addLike(long filmId, long userId) {
        filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film not found"));
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        filmRepository.addLike(filmId, userId);

        return filmRepository.getFilmById(filmId)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Film not found after like"));
    }

    public FilmDto removeLike(long filmId, long userId) {
        Film film = filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id " + filmId + " not found"));

        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        film.getLikes().remove(userId);
        filmRepository.updateFilm(film);
        log.info("User {} removed like from film {}", userId, filmId);
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getMostPopularFilms(Integer count, Integer genreId, Integer year) {
        List<Film> films = filmRepository.getPopularFilms(count, genreId, year);

        if (films.isEmpty()) {
            log.warn("No films found with the specified criteria");
        }

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public void removeFilm(long filmId) {
        if (!filmRepository.deleteFilm(filmId)) {
            throw new NotFoundException("Film with id " + filmId + " not found");
        }

    }

    public boolean filmExists(long filmId) {
        return filmRepository.getFilmById(filmId).isPresent();
    }

    private void validateDirectors(Set<Director> directors) {
        if (directors != null && !directors.isEmpty()) {
            directors.forEach(director -> {
                if (director.getId() == null) {
                    throw new ValidationException("Director id is null");
                }
            });

            Set<Long> directorsIds = directors.stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());

            if (!directorRepository.existAllByIds(directorsIds)) {
                throw new NotFoundException("One or more directors not found");
            }
        }
    }

    public List<FilmDto> getFilmsByDirector(long directorId, String sortBy) {
        List<Film> films = filmRepository.getFilmsByDirector(directorId, OrderBy.fromParam(sortBy).getColumn());
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    // Поиск
    public List<FilmDto> searchFilms(String query, String by) {
        log.info("Searching films with query: '{}', by: '{}'", query, by);

        if (query == null || query.trim().isEmpty()) {
            log.warn("Search query is empty or null");
            return List.of();
        }

        // Валидация параметра by
        if (by != null && !by.trim().isEmpty()) {
            String[] searchTypes = by.split(",");
            for (String searchType : searchTypes) {
                String trimmedType = searchType.trim();
                if (!trimmedType.equals("title") && !trimmedType.equals("director")) {
                    throw new ValidationException("Invalid search parameter: " + trimmedType +
                            ". Allowed values: 'title', 'director'");
                }
            }
        }

        List<Film> films = filmRepository.searchFilms(query, by);

        return films.stream()
                .map(FIlmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

}