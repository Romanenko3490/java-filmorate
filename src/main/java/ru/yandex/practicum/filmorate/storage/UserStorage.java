package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface UserStorage {

    Collection<User> getUsers();

    Optional<User> getUser(long id);

    User addUser(User newUser);

    User updateUser(User newUser);

    Map<Long, User> getUsersMap();

}
