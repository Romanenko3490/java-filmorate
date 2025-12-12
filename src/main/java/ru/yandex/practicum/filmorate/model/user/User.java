package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;

    @Email(message = "Invalid e-mail format")
    @NotBlank(message = "Email can not be blank or empty")
    private String email;

    @NotBlank(message = "login can not be blank or null")
    @Pattern(regexp = "\\S+", message = "Login can not contain spaces")
    private String login;

    private String name;

    @Past(message = "Birthday can not be in future")
    private LocalDate birthday;

    private Set<Long> friendList = new HashSet<>();

    public User(Long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = (name == null || name.isBlank()) ? login : name;
        this.birthday = birthday;
    }

    public User() {
    }
}
