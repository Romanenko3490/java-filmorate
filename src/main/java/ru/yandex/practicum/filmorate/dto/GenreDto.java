package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class GenreDto {
    private Long id;
    private String name;

    public GenreDto() {
    }

    public GenreDto(Long id) {
        this.id = id;
        this.name = null;
    }

    public GenreDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}