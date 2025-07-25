package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@Primary
public class UserRepository extends BaseRepository implements UserStorage {
    // SQL Queries
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    // Query methods
    @Override
    public List<User> getUsers() {
        return findAll(FIND_ALL_QUERY);
    }

    public Optional<User> findByEmail(String email) {
        return findById(FIND_BY_EMAIL_QUERY, email);
    }

    @Override
    public Optional<User> getUser(long userId) {
        try {
            return findById(FIND_BY_ID_QUERY, userId);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    // Modification methods
    @Override
    public User addUser(User user) {
        validateUserNotNull(user);

        long userId = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(userId);

        log.debug("Saved user with id: {}", userId);
        return user;
    }

    @Override
    public User updateUser(User user) {
        validateUserNotNull(user);

        int rowsUpdated = jdbc.update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (rowsUpdated == 0) {
            throw new NotFoundException("User with id " + user.getId() + " not found");
        }

        log.debug("Updated user with id: {}", user.getId());
        return user;
    }

    // Utility methods
    @Override
    public Map<Long, User> getUsersMap() {
        return Map.of();
    }

    private void validateUserNotNull(User user) {
        if (user == null) {
            log.error("Attempted to process null user");
            throw new IllegalArgumentException("User cannot be null");
        }
    }
}