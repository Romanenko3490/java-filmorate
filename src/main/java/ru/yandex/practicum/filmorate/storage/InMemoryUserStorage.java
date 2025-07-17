package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private Map<Long, User> users = new HashMap<>();

    public Collection<User> getUsers() {
        return users.values();
    }

    public Optional<User> getUser(long id) {
        log.info("Get user with id {}", id);
        return Optional.ofNullable(users.get(id))
                .map(user -> {
                    log.info("Get user with id {}", id);
                    return user;
                })
                .or(() -> {
                    log.info("No user with id {}", id);
                    throw new NotFoundException("No user with id " + id);
                });
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

    private long getNextUserId() {
        long maxId = users.values().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0);
        return ++maxId;
    }


    public Map<Long, User> getUsersMap() {
        Map<Long, User> mapToReturn = new HashMap<>(users);
        return mapToReturn;
    }
}
