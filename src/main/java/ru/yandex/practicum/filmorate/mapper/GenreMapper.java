package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.film.Genre;

public class GenreMapper {
    public static GenreDto mapToGenreDto(Genre genre) {
        if (genre == null) {
            return null;
        }

        return GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public static Genre toGenre(GenreDto dto) {
        if (dto == null) {
            return null;
        }

        return new Genre(
                dto.getId() != null ? dto.getId() : 0L,
                dto.getName()
        );
    }
}