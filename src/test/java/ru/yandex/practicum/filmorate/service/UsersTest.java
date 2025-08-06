package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbService.class,
        UserRepository.class,
        FriendshipRepository.class,
        FilmRepository.class,
        ReviewRepository.class,
        EntityChecker.class,
        EntityCheckService.class,
        FilmRowMapper.class,
        ReviewRowMapper.class,
        UserRowMapper.class})
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UsersTest {

    @Autowired
    private UserDbService userDbService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
        // Очищаем только таблицы, которые не содержат тестовых данных
        jdbcTemplate.update("DELETE FROM friendship");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM reviews");
        jdbcTemplate.update("DELETE FROM films");


        // Инициализация тестовых данных для других тестов
        newUserRequest = new NewUserRequest();
        newUserRequest.setEmail("test@example.com");
        newUserRequest.setLogin("testlogin");
        newUserRequest.setName("Test User");
        newUserRequest.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @BeforeEach
    void checkTestData() {
        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        Integer filmCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM films", Integer.class);
        Integer likeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_likes", Integer.class);

        log.info("Test data: {} users, {} films, {} likes", userCount, filmCount, likeCount);
        System.out.println("userCount: " + userCount + ", filmCount: " + filmCount + ", likeCount: " + likeCount);
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
        jdbcTemplate.update("DELETE FROM users");
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

    @Test
    void shouldDeleteUserAndFriendshipCascade() {
        UserDto user = userDbService.createUser(newUserRequest);

        newUserRequest.setEmail("friend@email.com");
        newUserRequest.setLogin("friendlogin");
        UserDto friend = userDbService.createUser(newUserRequest);

        userDbService.addFriend(user.getId(), friend.getId());
        userDbService.confirmFriend(user.getId(), friend.getId());

        userDbService.deleteUser(user.getId());

        assertThrows(NotFoundException.class, () -> userDbService.getUserById(user.getId()));

        assertThat(countUserFriendships(user.getId()))
                .as("Friendships should be deleted")
                .isZero();

        assertThat(countUserFriendships(friend.getId()))
                .as("Backlink Friendships should be deleted")
                .isZero();
    }

    @Test
    void shouldDeleteUserWithReviewsAndLikes() {
        UserDto user = userDbService.createUser(newUserRequest);

        jdbcTemplate.update("INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                        "VALUES (?, ?, ?, ?, ?)",
                "Test Film", "Desc", LocalDate.now(), 120, 1);

        Long filmId = jdbcTemplate.queryForObject("SELECT film_id FROM films LIMIT 1", Long.class);

        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)",
                filmId, user.getId());

        jdbcTemplate.update("INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                        "VALUES (?, ?, ?, ?, ?)",
                "Great film", true, user.getId(), filmId, 0);

        userDbService.deleteUser(user.getId());

        assertThat(countUserLikes(user.getId()))
                .as("Likes should be deleted")
                .isZero();

        assertThat(countUserReviews(user.getId()))
                .as("Reviews should be deleted")
                .isZero();
    }


    private int countUserFriendships(long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendship WHERE user_id = ? OR friend_id = ?",
                Integer.class,
                userId, userId
        );
        return count == null ? 0 : count;
    }

    private int countUserLikes(long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE user_id = ?",
                Integer.class,
                userId
        );
        return count == null ? 0 : count;
    }

    private int countUserReviews(long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reviews WHERE user_id = ?",
                Integer.class,
                userId);
        return count != null ? count : 0;
    }

    @Test
    void shouldReturnMultipleRecommendationsInCorrectOrder() {
        // Очистка данных
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        // Добавление пользователей
        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) VALUES (1, 'user1@example.com', 'login1', 'User One', '1990-01-01')");
        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) VALUES (2, 'user2@example.com', 'login2', 'User Two', '1995-01-01')");
        jdbcTemplate.update("INSERT INTO users (user_id, email, login, name, birthday) VALUES (3, 'user3@example.com', 'login3', 'User Three', '2000-01-01')");

        // Добавление фильмов
        jdbcTemplate.update("INSERT INTO films (film_id, name, description, release_date, duration, mpa_rating_id) VALUES (1, 'Film 1', 'Description 1', CURRENT_DATE, 120, 1)");
        jdbcTemplate.update("INSERT INTO films (film_id, name, description, release_date, duration, mpa_rating_id) VALUES (2, 'Film 2', 'Description 2', CURRENT_DATE, 90, 2)");
        jdbcTemplate.update("INSERT INTO films (film_id, name, description, release_date, duration, mpa_rating_id) VALUES (3, 'Film 3', 'Description 3', CURRENT_DATE, 150, 3)");
        jdbcTemplate.update("INSERT INTO films (film_id, name, description, release_date, duration, mpa_rating_id) VALUES (4, 'Film 4', 'Description 4', CURRENT_DATE, 100, 4)");

        // Добавление лайков
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (1, 1)");
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (2, 1)");
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (1, 2)");
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (3, 2)");
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (4, 2)");
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (3, 3)");

        List<FilmDto> recommendations = userDbService.getRecommendations(1);

        assertThat(recommendations)
                .as("Проверка рекомендаций для User1")
                .hasSize(2)
                .extracting(FilmDto::getId)
                .containsExactly(3L, 4L); // Ожидаемый порядок рекомендаций
    }

    @Test
    void shouldReturnEmptyListWhenNoRecommendations() {
        jdbcTemplate.update("INSERT INTO films (film_id, name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)", 1, "Film 1", "Description 1", LocalDate.now(), 120, 1);

        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", 1, 1);

        List<FilmDto> recommendations = userDbService.getRecommendations(1);

        assertThat(recommendations).isEmpty();
    }

}