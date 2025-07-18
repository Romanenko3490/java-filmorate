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
        // Очищаем таблицы перед тестом
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
        // 1. Создаем пользователя
        UserDto createdUser = userDbService.createUser(newUserRequest);

        // 2. Готовим данные для обновления
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setId(createdUser.getId()); // Используем реальный ID
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setLogin("newlogin");
        updateRequest.setName("New Name");
        updateRequest.setBirthday(LocalDate.of(1995, 1, 1));

        // 3. Обновляем пользователя
        UserDto updatedUser = userDbService.updateUser(createdUser.getId(), updateRequest);

        // 4. Проверяем обновленные данные
        assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(updatedUser.getEmail()).isEqualTo("newemail@example.com");
        assertThat(updatedUser.getLogin()).isEqualTo("newlogin");
        assertThat(updatedUser.getName()).isEqualTo("New Name");

        // 5. Проверяем, что данные сохранились в БД
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
        // Создаем пользователей
        UserDto user1 = userDbService.createUser(newUserRequest);
        newUserRequest.setEmail("friend@example.com");
        newUserRequest.setLogin("friendlogin");
        UserDto user2 = userDbService.createUser(newUserRequest);

        // 1. Добавляем друга (статус PENDING)
        userDbService.addFriend(user1.getId(), user2.getId());

        // 2. Подтверждаем дружбу
        userDbService.confirmFriend(user2.getId(), user1.getId());

        // 3. Проверяем список друзей (теперь должен содержать подтверждённого друга)
        assertThat(userDbService.getFriends(user1.getId()))
                .extracting(UserDto::getId)
                .containsExactly(user2.getId());

        // 4. Удаляем друга
        userDbService.removeFriend(user1.getId(), user2.getId());

        // 5. Проверяем, что список друзей пуст
        assertThat(userDbService.getFriends(user1.getId())).isEmpty();
    }

    @Test
    void shouldGetCommonFriends() {
        // Создаем трех пользователей
        UserDto user1 = userDbService.createUser(newUserRequest);

        newUserRequest.setEmail("friend1@example.com");
        newUserRequest.setLogin("friend1login");
        UserDto user2 = userDbService.createUser(newUserRequest);

        newUserRequest.setEmail("friend2@example.com");
        newUserRequest.setLogin("friend2login");
        UserDto user3 = userDbService.createUser(newUserRequest);

        // user1 и user2 дружат с user3
        userDbService.addFriend(user1.getId(), user3.getId());
        userDbService.addFriend(user2.getId(), user3.getId());

        // Проверяем общих друзей
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
        // Создаем первого пользователя
        userDbService.createUser(newUserRequest);

        // Пытаемся создать второго пользователя с тем же email
        NewUserRequest duplicateRequest = new NewUserRequest();
        duplicateRequest.setEmail(newUserRequest.getEmail());
        duplicateRequest.setLogin("differentlogin"); // Другой логин
        duplicateRequest.setName("Different Name");
        duplicateRequest.setBirthday(LocalDate.now());

        // Ожидаем наше кастомное исключение
        assertThrows(ru.yandex.practicum.filmorate.exception.ValidationException.class,
                () -> userDbService.createUser(duplicateRequest));
    }

    @Test
    void shouldConfirmFriendship() {
        // 1. Создаем пользователей
        UserDto user1 = userDbService.createUser(newUserRequest);
        newUserRequest.setEmail("friend@example.com");
        UserDto user2 = userDbService.createUser(newUserRequest);

        // 2. Проверяем начальное состояние
        assertThat(userDbService.getFriends(user1.getId())).isEmpty();
        assertThat(userDbService.getFriends(user2.getId())).isEmpty();

        // 3. User1 отправляет заявку User2
        userDbService.addFriend(user1.getId(), user2.getId());

        // 4. Проверяем pending-запросы
        assertThat(userDbService.getPendingRequests(user2.getId()))
                .hasSize(1)
                .extracting(UserDto::getId)
                .containsExactly(user1.getId());

        // 5. User2 подтверждает дружбу
        userDbService.confirmFriend(user2.getId(), user1.getId());

        // 6. Проверяем подтверждённых друзей
        assertThat(userDbService.getFriends(user1.getId()))
                .hasSize(1)
                .extracting(UserDto::getId)
                .containsExactly(user2.getId());

        assertThat(userDbService.getFriends(user2.getId()))
                .hasSize(1)
                .extracting(UserDto::getId)
                .containsExactly(user1.getId());

        // 7. Дополнительная проверка в БД
        Integer friendshipCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE " +
                        "((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) " +
                        "AND status = 'CONFIRMED'",
                Integer.class,
                user1.getId(), user2.getId(),
                user2.getId(), user1.getId()
        );
        assertThat(friendshipCount).isEqualTo(2);
    }
}