package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private Integer id;

    @Email(message = "Invalid e-mail format")
    @NotNull(message = "Email can not be empty")
    @NotBlank(message = "Email can not be blank")
    private String email;

    @NotNull(message = "Login can not be empty")
    @NotBlank(message = "login can not be blank")
    private String login;

    private String name;

    @Past(message = "Birthday can not be in future")
    private LocalDate birthday;
}
