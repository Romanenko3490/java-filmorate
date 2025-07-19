package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NewUserRequest {

    @Email(message = "Invalid e-mail format")
    @NotBlank(message = "Email can not be blank or empty")
    private String email;

    @NotNull(message = "Login can not be empty")
    @NotBlank(message = "login can not be blank")
    @Pattern(regexp = "\\S+", message = "Login can not contain spaces")
    private String login;

    private String name;

    @Past(message = "Birthday can not be in future")
    private LocalDate birthday;
}
