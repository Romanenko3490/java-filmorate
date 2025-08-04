package ru.yandex.practicum.filmorate.model.film;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Genre {
    private long id;
    private String name;

    public Genre(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Genre() {
    }
}
