package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserDbService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Primary
public class UserDbController {
    private final UserDbService userDbService;
    private final FeedService feedService;

    @GetMapping
    public Collection<UserDto> getUsers() {
        return userDbService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable long userId) {

        return userDbService.getUserById(userId);
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody NewUserRequest request) {
        return userDbService.createUser(request);
    }

    @PutMapping()
    public UserDto updateUser(@RequestBody UpdateUserRequest request) {
        return userDbService.updateUser(request.getId(), request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long userId) {
        userDbService.deleteUser(userId);
    }

    //friendship
    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(
            @PathVariable long userId,
            @PathVariable long friendId) {
        userDbService.addFriend(userId, friendId);
        feedService.addFriendEvent(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFriend(
            @PathVariable long userId,
            @PathVariable long friendId) {
        userDbService.removeFriend(userId, friendId);
        feedService.removeFriendEvent(userId, friendId);
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

    //feed
    @GetMapping("/{userId}/feed")
    public List<FeedEventDto> getFeeds(@PathVariable long userId) {
        return feedService.getFeedByUserId(userId);
    }

    //Recommendations
    @GetMapping("/{userId}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable long userId) {
        return userDbService.getRecommendations(userId);
    }

}
