package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;


    public boolean isEmptyRequest() {
        return id == 0 && !hasEmail() && !hasLogin() && !hasName() && !hasBirthday();
    }

    public boolean hasEmail() {
        return !(email == null || email.isBlank());
    }

    public boolean hasLogin() {
        return !(login == null || login.isBlank());
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasBirthday() {
        return !(birthday == null || birthday.isAfter(LocalDate.now()));
    }


}
