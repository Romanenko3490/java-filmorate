package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
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
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Primary
@RequiredArgsConstructor
public class FilmDbService {
    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final DirectorRepository directorRepository;
    private final EntityCheckService entityCheckService;


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
        if (request.getMpa() != null && !request.getMpa().isEmpty()) {
            Set<Long> mpaIds = request.getMpa().stream()
                    .map(MpaRating::getId)
                    .collect(Collectors.toSet());

            Set<Long> existingMpaIds = mpaRepository.findAllExistingIds(mpaIds);

            Set<Long> missingMpaIds = mpaIds.stream()
                    .filter(id -> !existingMpaIds.contains(id))
                    .collect(Collectors.toSet());

            if (!missingMpaIds.isEmpty()) {
                throw new NotFoundException("Genres with ids " + missingMpaIds + " not found");
            }
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
        entityCheckService.checkFilmExists(filmId);
        Film film = filmRepository.getFilmById(filmId).get();

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
        entityCheckService.checkFilmExists(filmId);
        entityCheckService.checkUserExists(userId);

        filmRepository.addLike(filmId, userId);

        return filmRepository.getFilmById(filmId)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Film not found after like"));
    }

    public FilmDto removeLike(long filmId, long userId) {
        entityCheckService.checkFilmExists(filmId);
        entityCheckService.checkUserExists(userId);

        Film film = filmRepository.getFilmById(filmId).get();
        film.getLikes().remove(userId);
        filmRepository.updateFilm(film);
        filmRepository.removeLike(filmId, userId);
        log.info("User {} removed like from film {}", userId, filmId);
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getMostPopularFilms(Integer count, Integer genreId, Integer year) {
        if (genreId != null && year != null) {
            if (genreId <= 0 || genreId > 6) {
                throw new IllegalArgumentException("Genre id must be between 1 and 6");
            }
        }

        if (year != null) {
            if (year == null || year < 1895 || year > LocalDate.now().getYear()) {
                throw new IllegalArgumentException("Year must be between 1895 and 1980");
            }
        }

        List<Film> films = filmRepository.getPopularFilms(count, genreId, year);
        if (films.isEmpty()) {
            log.warn("No films found with the specified criteria");
        }

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public void removeFilm(long filmId) {
        entityCheckService.checkFilmExists(filmId);
        filmRepository.deleteFilm(filmId);
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
        entityCheckService.checkDirectorExists(directorId);
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
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getCommonFilms(long userId, long friendId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkUserExists(friendId);

        List<Film> commonFilms = filmRepository.getCommonFilms(userId, friendId);
        return commonFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

}