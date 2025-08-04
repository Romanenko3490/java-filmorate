package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class MpaDto {
    private Long id;

    public MpaDto() {
    }

    public MpaDto(Long id) {
        this.id = id;
    }

}
