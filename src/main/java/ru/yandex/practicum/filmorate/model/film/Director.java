package ru.yandex.practicum.filmorate.model.film;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Director {
    private Long directorId;
    private String name;
}
