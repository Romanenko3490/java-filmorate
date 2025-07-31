package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.film.Director;

public class DirectorMapper {
    public static DirectorDto mapToDirectorDto(Director director) {
        return new DirectorDto(director.getId(), director.getName());
    }
}