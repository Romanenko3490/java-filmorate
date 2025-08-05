package ru.yandex.practicum.filmorate.model.film;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class MpaRating implements Comparable<MpaRating>{
    private Long id;
    private String name;
    private String description;

    public MpaRating() {
    }

    public MpaRating(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public int compareTo(MpaRating o) {
        return this.id.compareTo(o.getId());
    }
}