package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserDbService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Primary
public class UserDbController {
    private final UserDbService userDbService;

    @Autowired
    public UserDbController(UserDbService userDbService) {
        this.userDbService = userDbService;
    }

    @GetMapping
    public Collection<UserDto> getUsers() {
        return userDbService.getAllUsers();
    }

    @GetMapping("/{user_id}")
    public UserDto getUser(@PathVariable long user_id) {

        return userDbService.getUserById(user_id);
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody NewUserRequest request) {
        return userDbService.createUser(request);
    }

    @PutMapping()
    public UserDto updateUser(@RequestBody UpdateUserRequest request) {
        return userDbService.updateUser(request.getId(), request);
    }

    //friendship
    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(
            @PathVariable long userId,
            @PathVariable long friendId) {
        userDbService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFriend(
            @PathVariable long userId,
            @PathVariable long friendId) {
        userDbService.removeFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<UserDto> getFriends(@PathVariable long userId) {
        return userDbService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(
            @PathVariable long userId,
            @PathVariable long otherId) {
        return userDbService.getCommonFriends(userId, otherId);
    }


}
