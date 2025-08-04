package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.FriendshipRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Primary
@RequiredArgsConstructor
public class UserDbService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FilmRepository filmRepository;
    private final EntityCheckService entityCheckService;

    public UserDto createUser(NewUserRequest request) {
        validateUserRequest(request);
        checkEmailUniqueness(request.getEmail());

        User user = UserMapper.mapToUser(request);
        normalizeUserName(user);

        user = userRepository.addUser(user);
        log.info("Created user: {}", user);
        return UserMapper.mapToUserDto(user);
    }

    public UserDto getUserById(long userId) {
        entityCheckService.checkUserExists(userId);
        return UserMapper.mapToUserDto(userRepository.getUser(userId).get());
    }

    public List<UserDto> getAllUsers() {
        return userRepository.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(long userId, UpdateUserRequest request) {
        entityCheckService.checkUserExists(userId);
        User existingUser = userRepository.getUser(userId).get();
        validateUpdateRequest(request, userId);

        User updatedUser = UserMapper.updateFields(existingUser, request);
        userRepository.updateUser(updatedUser);

        return UserMapper.mapToUserDto(updatedUser);
    }

    public void deleteUser(long userId) {
        if (!userRepository.deleteUser(userId)) {
            throw new NotFoundException("User not found");
        }
    }

    // Friendship operations
    public void addFriend(long userId, long friendId) {
        validateUsers(userId, friendId);
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkUserExists(friendId);

        if (friendshipRepository.friendshipExists(userId, friendId)) {
            throw new IllegalStateException("Friendship already exists");
        }

        friendshipRepository.addFriend(userId, friendId);
        log.info("User {} added user {} as friend", userId, friendId);
    }

    public void confirmFriend(long userId, long friendId) {
        validateFriendshipOperation(userId, friendId);
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkUserExists(friendId);
        friendshipRepository.confirmFriend(userId, friendId);
        log.info("User {} confirmed friendship with {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        validateUsers(userId, friendId);
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkUserExists(friendId);
        friendshipRepository.removeFriend(userId, friendId);
        log.info("User {} removed user {} from friends", userId, friendId);
    }

    // Friendship queries
    public List<UserDto> getFriends(long userId) {
        entityCheckService.checkUserExists(userId);
        return friendshipRepository.getFriends(userId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getPendingRequests(long userId) {
        entityCheckService.checkUserExists(userId);
        return friendshipRepository.getPendingRequests(userId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getCommonFriends(long userId, long otherId) {
        validateUsers(userId, otherId);
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkUserExists(otherId);
        return friendshipRepository.getCommonFriends(userId, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    // Validation methods
    private void validateUserRequest(NewUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ValidationException("Email cannot be empty");
        }
        if (request.getLogin() == null || request.getLogin().isBlank() || request.getLogin().contains(" ")) {
            throw new ValidationException("Login cannot be empty or contain spaces");
        }
        if (request.getBirthday() != null && request.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Birthday cannot be in future");
        }
    }

    private void validateFriendshipOperation(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Cannot perform operation with yourself");
        }
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkUserExists(friendId);
    }


    private void checkEmailUniqueness(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("Email already exists");
        }
    }

    private void normalizeUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateUpdateRequest(UpdateUserRequest request, long userId) {
        if (request.getId() != 0 && request.getId() != userId) {
            throw new ValidationException("ID in path and body mismatch");
        }
        if (request.hasBirthday() && request.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Birthday cannot be in future");
        }
    }

    private void validateUsers(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Cannot add yourself as friend");
        }
    }

    //Recommendations
    public List<FilmDto> getRecommendations(long userId) {
        entityCheckService.checkUserExists(userId);
        List<Film> recommendedFilms = filmRepository.getRecommendedFilms(userId);

        return recommendedFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
