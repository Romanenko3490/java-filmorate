
package ru.yandex.practicum.filmorate.service;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User newUser) {
        if (newUser == null) {
            log.error("User is null");
            throw new ValidationException("User is null");
        }
        try {
            log.info("Adding new user: email={}, login={}", newUser.getEmail(), newUser.getLogin());
            return userStorage.addUser(newUser);
        } catch (ValidationException e) {
            log.error("Validation failed: {}", e.getMessage());
            throw e;
        }
    }

    public User getUser(@NotBlank Integer id) {
        return userStorage.getUser(id);
    }

    public User updateUser(User newUser) {
        if (newUser == null) {
            log.error("Updated user is null");
            throw new ValidationException("Updated user is null");
        }
        log.info("Updating user: email={}, login={}", newUser.getEmail(), newUser.getLogin());
        if (newUser.getId() == null) {
            throw new ValidationException("id is null");
        }

        if (userStorage.getUsersMap().containsKey(newUser.getId())) {
            return userStorage.updateUser(newUser);
        }
        log.error("User id={} not dound", newUser.getId());
        throw new NotFoundException("user with id = " + newUser.getId() + " not found");

    }

    public void addFriend(@NotBlank @Positive Integer userId,
                          @NotBlank @Positive Integer friendId) {

        isPresentInStorage(userId, friendId);

        userStorage.getUsersMap().get(userId).getFriendList().add(friendId);
        userStorage.getUsersMap().get(friendId).getFriendList().add(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(@NotBlank @Positive Integer userId,
                             @NotBlank @Positive Integer friendId) {

        isPresentInStorage(userId, friendId);

        userStorage.getUsersMap().get(userId).getFriendList().remove(friendId);
        userStorage.getUsersMap().get(friendId).getFriendList().remove(userId);

        log.info("Пользователи {} и {} теперь не друзья", userId, friendId);
    }

    public Collection<User> getFriends(@NotBlank @Positive Integer userId) {

        isPresentInStorage(userId);

        User user = userStorage.getUsersMap().get(userId);
        return user.getFriendList().stream()
                .map(userStorage::getUser)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(@NotBlank @Positive Integer userId,
                                       @NotBlank @Positive Integer otherUserId) {

        isPresentInStorage(userId, otherUserId);

        Set<Integer> userFriendsId = new HashSet<>(userStorage.getUser(userId).getFriendList());
        Set<Integer> otherUserFriendsId = new HashSet<>(userStorage.getUser(otherUserId).getFriendList());

        userFriendsId.retainAll(otherUserFriendsId);

        return userFriendsId.stream()
                .map(userStorage::getUser)
                .collect(Collectors.toList());
    }

    private void isPresentInStorage(@NotBlank @Positive Integer userId) {
        if (!userStorage.getUsersMap().containsKey(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + "не найден");
        }
    }

    private void isPresentInStorage(@NotBlank @Positive Integer userId,
                                    @NotBlank @Positive Integer otherUserId) {
        if (!userStorage.getUsersMap().containsKey(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + "не найден");
        }
        if (!userStorage.getUsersMap().containsKey(otherUserId)) {
            throw new NotFoundException("Друг с id " + otherUserId + "не найден");
        }
    }

}
