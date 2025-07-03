package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;
import java.util.Map;

public interface UserStorage {

    Collection<User> getUsers();

    User getUser(int id);

    User addUser(User newUser);

    User updateUser(User newUser);

    Map<Integer, User> getUsersMap();

}
