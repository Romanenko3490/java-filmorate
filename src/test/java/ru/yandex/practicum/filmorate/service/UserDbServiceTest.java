package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.FriendshipRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbService.class,
        UserRepository.class,
        FriendshipRepository.class,
        UserRowMapper.class})
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserDbServiceTest {

    @Autowired
    private UserDbService userDbService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private NewUserRequest newUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        // Полная очистка всех связанных таблиц
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM films"); // если есть связи

        // Инициализация тестовых данных
        newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("test@example.com");
        newUserRequest.setLogin("testlogin");
        newUserRequest.setName("Test User");
        newUserRequest.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldCreateAndGetUser() {
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM users");

        UserDto createdUser = userDbService.createUser(newUserRequest);
        UserDto retrievedUser = userDbService.getUserById(createdUser.getId());

        assertThat(retrievedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(retrievedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(retrievedUser.getLogin()).isEqualTo("testlogin");
        assertThat(retrievedUser.getName()).isEqualTo("Test User");
    }

    @Test
    @Transactional
    void shouldUpdateUser() {
        UserDto createdUser = userDbService.createUser(newUserRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setId(createdUser.getId()); // Используем реальный ID
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setLogin("newlogin");
        updateRequest.setName("New Name");
        updateRequest.setBirthday(LocalDate.of(1995, 1, 1));

        UserDto updatedUser = userDbService.updateUser(createdUser.getId(), updateRequest);

        assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(updatedUser.getLogin()).isEqualTo("newlogin");
        assertThat(updatedUser.getName()).isEqualTo("New Name");

        UserDto dbUser = userDbService.getUserById(createdUser.getId());
        assertThat(dbUser.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    void shouldGetAllUsers() {
        userDbService.createUser(newUserRequest);

        newUserRequest.setEmail("another@example.com");
        newUserRequest.setLogin("anotherlogin");
        userDbService.createUser(newUserRequest);

        List<UserDto> users = userDbService.getAllUsers();
        assertThat(users).hasSize(2);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userDbService.getUserById(999L));
    }

    @Test
    void shouldAddAndRemoveFriend() {
        UserDto user1 = userDbService.createUser(newUserRequest);
        newUserRequest.setEmail("friend@example.com");
        newUserRequest.setLogin("friendlogin");
        UserDto user2 = userDbService.createUser(newUserRequest);

        userDbService.addFriend(user1.getId(), user2.getId());

        userDbService.confirmFriend(user2.getId(), user1.getId());

        assertThat(userDbService.getFriends(user1.getId()))
                .extracting(UserDto::getId)
                .containsExactly(user2.getId());

        userDbService.removeFriend(user1.getId(), user2.getId());

        assertThat(userDbService.getFriends(user1.getId())).isEmpty();
    }

    @Test
    void shouldGetCommonFriends() {
        UserDto user1 = userDbService.createUser(newUserRequest);

        newUserRequest.setEmail("friend1@example.com");
        newUserRequest.setLogin("friend1login");
        UserDto user2 = userDbService.createUser(newUserRequest);

        newUserRequest.setEmail("friend2@example.com");
        newUserRequest.setLogin("friend2login");
        UserDto user3 = userDbService.createUser(newUserRequest);

        userDbService.addFriend(user1.getId(), user3.getId());
        userDbService.addFriend(user2.getId(), user3.getId());

        List<UserDto> commonFriends = userDbService.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).extracting(UserDto::getId).containsExactly(user3.getId());
    }

    @Test
    void shouldThrowWhenAddingSelfAsFriend() {
        UserDto user = userDbService.createUser(newUserRequest);

        assertThrows(ValidationException.class,
                () -> userDbService.addFriend(user.getId(), user.getId()));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        newUserRequest.setName("");
        UserDto user = userDbService.createUser(newUserRequest);

        assertThat(user.getName()).isEqualTo(newUserRequest.getLogin());
    }

    @Test
    void shouldThrowWhenEmailExists() {
        userDbService.createUser(newUserRequest);

        // Пытаемся создать второго пользователя с тем же email
        NewUserRequest duplicateRequest = new NewUserRequest();
        duplicateRequest.setEmail(newUserRequest.getEmail());
        duplicateRequest.setLogin("differentlogin"); // Другой логин
        duplicateRequest.setName("Different Name");
        duplicateRequest.setBirthday(LocalDate.now());

        assertThrows(ru.yandex.practicum.filmorate.exception.ValidationException.class,
                () -> userDbService.createUser(duplicateRequest));
    }

}