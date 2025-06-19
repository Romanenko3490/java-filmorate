package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;

@Component
public interface UserStorage {

    Collection<User> getUsers();

    User getUser(int id);

    User addUser(User newUser);

    User updateUser(User newUser);

    Map<Integer, User> getUsersMap();

}
