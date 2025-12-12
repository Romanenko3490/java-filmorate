package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

public class MpaMapper {
    public static MpaDto mapToMpaDto(MpaRating mpa) {
        MpaDto dto = new MpaDto(mpa.getId(), mpa.getName());
        return dto;
    }
}
