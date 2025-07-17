package ru.yandex.practicum.filmorate.model.film;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Genre {
    private int id;
    private String name;

    // Конструктор для работы с БД
    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Пустой конструктор для Spring
    public Genre() {}
}
