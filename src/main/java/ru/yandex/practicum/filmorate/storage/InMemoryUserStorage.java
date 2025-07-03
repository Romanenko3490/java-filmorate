package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//Всю логику валидации поместил на сервисный слой, поэтому большинство тестов стало для класса не актуально

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private Map<Integer, User> users = new HashMap<>();

    public Collection<User> getUsers() {
        return users.values();
    }

    public User getUser(int id) {
        log.info("Get user with id {}", id);
        if (!users.containsKey(id)) {
            log.error("User with id {} not found", id);
            throw new NotFoundException("User with id " + id + " not found");
        }
        return users.get(id);
    }

    public User addUser(User newUser) {
        newUser.setId(getNextUserId());
        users.put(newUser.getId(), newUser);
        log.info("New user created: id={}, login={}", newUser.getId(), newUser.getLogin());
        return newUser;
    }

    public User updateUser(User newUser) {
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

    private int getNextUserId() {
        int maxId = users.values().stream()
                .mapToInt(User::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }


    public Map<Integer, User> getUsersMap() {
        Map<Integer, User> mapToReturn = new HashMap<>(users);
        return mapToReturn;
    }
}
