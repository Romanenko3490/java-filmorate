package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
@Validated
public class UserController {

    private Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        log.info("Get user with id {}", id);
        if (!users.containsKey(id)) {
            log.error("User with id {} not found", id);
            throw new NotFoundException("User with id " + id + " not found");
        }
        return users.get(id);
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User newUser) {
        if (newUser == null) {
            log.error("User is null");
            throw new ValidationException("User is null");
        }
        try {
            log.info("Adding new user: email={}, login={}", newUser.getEmail(), newUser.getLogin());
            validateUser(newUser);
            newUser.setId(getNextUserId());
            users.put(newUser.getId(), newUser);
            log.info("New user created: id={}, login={}", newUser.getId(), newUser.getLogin());
            return newUser;
        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        if (newUser == null) {
            log.error("Updated user is null");
            throw new ValidationException("Updated user is null");
        }
        log.info("Updating user: email={}, login={}", newUser.getEmail(), newUser.getLogin());
        if (newUser.getId() == null) {
            throw new ValidationException("id is null");
        }
        validateUser(newUser);
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            oldUser.setName(newUser.getName());
            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            log.info("User updated: id={}, login={}", oldUser.getId(), oldUser.getLogin());
            return oldUser;
        }
        log.error("User id={} not dound", newUser.getId());
        throw new NotFoundException("user with id = " + newUser.getId() + " not found");
    }

    private int getNextUserId() {
        int maxId = users.values().stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().isBlank()) {
            log.error("Incorrect email(null / blank / not contains '@')");
            throw new ValidationException("Email cannot be empty and shell contain at least one @");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            log.error("Incorrect login(null / blank / not contains spaces)");
            throw new ValidationException("Login cannot be empty and shell not contain spaces");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Incorrect birthday(null) or is below now");
            throw new ValidationException("Birthday cannot be in future");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
