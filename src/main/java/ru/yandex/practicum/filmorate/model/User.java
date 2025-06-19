package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Integer id;

    @Email(message = "Invalid e-mail format")
    @NotBlank(message = "Email can not be blank or empty")
    private String email;

    @NotNull(message = "Login can not be empty")
    @NotBlank(message = "login can not be blank")
    private String login;

    private String name;

    @Past(message = "Birthday can not be in future")
    private LocalDate birthday;

    private Set<Integer> friendList = new HashSet<>();

    public User(Integer id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = (name == null || name.isBlank()) ? login : name;
        this.birthday = birthday;
    }
}
