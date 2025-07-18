package ru.yandex.practicum.filmorate.service;


import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Primary
public class UserDbService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @Autowired
    public UserDbService(UserRepository userRepository,
                         FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

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
        return UserMapper.mapToUserDto(getUserOrThrow(userId));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(long userId, UpdateUserRequest request) {
        User existingUser = getUserOrThrow(userId);
        validateUpdateRequest(request, userId);

        User updatedUser = UserMapper.updateFields(existingUser, request);
        userRepository.updateUser(updatedUser);

        return UserMapper.mapToUserDto(updatedUser);
    }

    // Friendship operations
    public void addFriend(long userId, long friendId) {
        validateUsers(userId, friendId);

        if (friendshipRepository.friendshipExists(userId, friendId)) {
            throw new IllegalStateException("Friendship already exists");
        }

        friendshipRepository.addFriend(userId, friendId);
        log.info("User {} added user {} as friend", userId, friendId);
    }

    public void confirmFriend(long userId, long friendId) {
        validateFriendshipOperation(userId, friendId);
        friendshipRepository.confirmFriend(userId, friendId);
        log.info("User {} confirmed friendship with {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        validateUsers(userId, friendId);
        friendshipRepository.removeFriend(userId, friendId);
        log.info("User {} removed user {} from friends", userId, friendId);
    }

    // Friendship queries
    public List<UserDto> getFriends(long userId) {
        getUserOrThrow(userId);
        return friendshipRepository.getFriends(userId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getPendingRequests(long userId) {
        getUserOrThrow(userId);
        return friendshipRepository.getPendingRequests(userId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getCommonFriends(long userId, long otherId) {
        validateUsers(userId, otherId);
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
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
    }

    private User getUserOrThrow(long userId) {
        return userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
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
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
    }


}
