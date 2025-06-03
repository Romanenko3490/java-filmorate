package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Film {
    private Integer id;

    @NotNull(message = "Film name cannot be empty")
    @NotBlank(message = "Film name cannot be blank")
    private String name;

    @Size(max = 200, message = "Film description cannot be longer than 200 characters")
    private String description;


    private LocalDate releaseDate;

    @NotNull(message = "Please complete field \"duration\"")
    @Positive(message = "Film duration cannot be negative or zero")
    private Integer duration;

}
