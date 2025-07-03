package ru.yandex.practicum.filmorate.model.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class Genre {
    private int id;
    private String name;

    //можно будет удалить после перехода на бд
    public Genre(String name) {
        GenreType genre = GenreType.fromName(name);
        this.id = genre.getId();
        this.name = genre.getName();
    }

    public void setName(String name) {
        GenreType genre = GenreType.fromName(name);
        this.id = genre.getId();
        this.name = genre.getName();
    }

    // Конструктор для работы с БД
    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

}


//Предполагается, что этот енум будет лежать в бд и потом из бд подгружаться  в будущем
@Getter
enum GenreType {
    COMEDY(1, "комедия"),
    DRAMA(2, "драма"),
    ANIMATION(3, "мультфильм"),
    THRILLER(4, "триллер"),
    DOCUMENTARY(5, "документальный"),
    ACTION(6, "боевик");

    private final int id;
    private final String name;

    GenreType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static GenreType fromName(String name) {
        for (GenreType genre : values()) {
            if (genre.name.equalsIgnoreCase(name)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Неизвестный жанр: " + name);
    }
}
