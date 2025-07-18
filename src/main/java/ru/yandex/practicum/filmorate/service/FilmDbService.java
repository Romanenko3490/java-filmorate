package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FIlmMapper;
import ru.yandex.practicum.filmorate.model.film.Film;


import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Primary
public class FilmDbService {
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;

    @Autowired
    public FilmDbService(
            UserRepository userRepository,
            FilmRepository filmRepository,
            MpaRepository mpaRepository,
            GenreRepository genreRepository) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.mpaRepository = mpaRepository;
        this.genreRepository = genreRepository;
    }


    public Collection<FilmDto> getAllFilms() {
        log.info("Getting all films");
        return filmRepository.getFilms().stream()
                .map(FIlmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilmById(long filmId) {
        log.info("Getting film by id: {}", filmId);
        return filmRepository.getFilmById(filmId)
                .map(FIlmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Film with id: " + filmId));
    }

    public FilmDto addFilm(NewFilmRequest request) {
        // Проверяем существование MPA перед добавлением
        if (!mpaRepository.existsById(request.getMpa().getId())) {
            throw new NotFoundException("MPA rating with id " + request.getMpa().getId() + " not found");
        }

        if (request.getGenres() != null) {
            request.getGenres().forEach(genre -> {
                if (!genreRepository.existsById(genre.getId())) {
                    throw new NotFoundException("Genre with id " + genre.getId() + " not found");
                }
            });
        }

        Film film = FIlmMapper.mapToFilm(request);
        filmRepository.addFilm(film);
        return FIlmMapper.mapToFilmDto(film);
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

        filmRepository.updateFilm(film);
        return FIlmMapper.mapToFilmDto(film);
    }

    public FilmDto addLike(long filmId, long userId) {
        // Проверяем существование фильма и пользователя
        filmRepository.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Film not found"));
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        filmRepository.addLike(filmId, userId);

        return filmRepository.getFilmById(filmId)
                .map(FIlmMapper::mapToFilmDto)
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
        return FIlmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getMostPopularFilms(Integer count) {
        List<Film> films = filmRepository.getPopularFilms(count);

        if (films.isEmpty()) {
            log.warn("No films with likes found");
        }

        return films.stream()
                .map(FIlmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}