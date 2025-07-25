package ru.yandex.practicum.filmorate.model.film;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class MpaRating {
    private int id;
    private String name;
    private String description;

    public MpaRating() {
    }

    public MpaRating(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}