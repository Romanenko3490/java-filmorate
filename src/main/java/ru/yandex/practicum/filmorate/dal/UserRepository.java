package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

    //Frindship

//    private static final String ADD_FRIEND_QUERY =
//            "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";
//    private static final String CHECK_FRIENDSHIP_EXISTS_QUERY =
//            "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
//    private static final String REMOVE_FRIEND_QUERY =
//            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
//    private static final String GET_FRIENDS_QUERY =
//            "SELECT u.* FROM users u " +
//                    "JOIN friendship f ON u.user_id = f.friend_id " +
//                    "WHERE f.user_id = ? AND f.status = 'CONFIRMED'";
//    private static final String GET_COMMON_FRIENDS_QUERY =
//            "SELECT u.* FROM users u " +
//                    "JOIN friendship f1 ON u.user_id = f1.friend_id " +
//                    "JOIN friendship f2 ON u.user_id = f2.friend_id " +
//                    "WHERE f1.user_id = ? AND f2.user_id = ? " +
//                    "AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'";
//    private static final String REMOVE_FRIENDSHIP_QUERY =
//            "DELETE FROM friendship WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";



    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    public List<User> getUsers() {
        return findAll(FIND_ALL_QUERY);
    }

    public Optional<User> findByEmail(String email) {
        return findById(FIND_BY_EMAIL_QUERY, email);
    }

    public Optional<User> getUser(long user_id) {
        try {
            return findById(FIND_BY_ID_QUERY, user_id);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public User addUser(User user) {
        if (user == null) {
            log.error("User is null");
            throw new IllegalArgumentException("User cannot be null");
        }
        long user_id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(user_id);
        log.debug("save user {}", user);
        return user;
    }

    public User updateUser(User user) {
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
        return user;
    }

    @Override
    public Map<Long, User> getUsersMap() {
        return Map.of();
    }

    //friendship methods
//    public boolean addFriend(long userId, long friendId) {
//        try {
//            jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
//            log.debug("User {} added friend {}", userId, friendId);
//            return true;
//        } catch (DuplicateKeyException e) {
//            log.debug("Friendship between {} and {} already exists", userId, friendId);
//            return false;
//        }
//    }
//
//    public void removeFriend(long userId, long friendId) {
//        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
//        log.debug("User {} removed friend {}", userId, friendId);
//    }
//
//    public List<User> getFriends(long userId) {
//        return jdbc.query(GET_FRIENDS_QUERY, mapper, userId);
//    }
//
//    public List<User> getCommonFriends(long userId, long otherId) {
//        return jdbc.query(GET_COMMON_FRIENDS_QUERY, mapper, userId, otherId);
//    }
//
//    public void addMutualFriendship(long userId, long friendId) {
//        // Добавляем основную дружбу (userId → friendId)
//        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
//
//        // Добавляем обратную дружбу (friendId → userId)
//        jdbc.update(ADD_FRIEND_QUERY, friendId, userId);
//
//        log.debug("Mutual friendship created between {} and {}", userId, friendId);
//    }
//
//    public boolean friendshipExists(long userId, long friendId) {
//        Integer count = jdbc.queryForObject(
//                CHECK_FRIENDSHIP_EXISTS_QUERY,
//                Integer.class,
//                userId,
//                friendId
//        );
//        return count != null && count > 0;
//    }
//
//    @Transactional
//    public boolean removeMutualFriendship(long userId, long friendId) {
//        int deleted = jdbc.update(REMOVE_FRIENDSHIP_QUERY,
//                userId, friendId,
//                friendId, userId);
//
//        boolean existed = deleted > 0;
//        log.debug("Mutual friendship between {} and {} {}removed",
//                userId, friendId, existed ? "" : "not found, nothing to ");
//        return existed;
//    }
}
