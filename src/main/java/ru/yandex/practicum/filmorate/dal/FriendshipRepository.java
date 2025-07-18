package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.List;

@Repository
public class FriendshipRepository {
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
    private static final String CONFIRM_FRIEND_QUERY =
            "UPDATE friendship SET status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?";
    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS_QUERY =
            "SELECT u.* FROM users u JOIN friendship f ON u.user_id = f.friend_id WHERE f.user_id = ?";
    private static final String GET_PENDING_REQUESTS_QUERY =
            "SELECT u.* FROM users u JOIN friendship f ON u.user_id = f.user_id " +
                    "WHERE f.friend_id = ? AND f.status = 'PENDING'";
    private static final String GET_COMMON_FRIENDS_QUERY =
            "SELECT u.* FROM users u JOIN friendship f1 ON u.user_id = f1.friend_id " +
                    "JOIN friendship f2 ON u.user_id = f2.friend_id WHERE f1.user_id = ? AND f2.user_id = ?";
    private static final String CHECK_FRIENDSHIP_QUERY =
            "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    public FriendshipRepository(JdbcTemplate jdbc, UserRowMapper userRowMapper) {
        this.jdbc = jdbc;
        this.userRowMapper = userRowMapper;
    }

    public void addFriend(long userId, long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
    }

    public void confirmFriend(long userId, long friendId) {
        jdbc.update(CONFIRM_FRIEND_QUERY, userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }

    public List<User> getFriends(long userId) {
        return jdbc.query(GET_FRIENDS_QUERY, userRowMapper, userId);
    }

    public List<User> getPendingRequests(long userId) {
        return jdbc.query(GET_PENDING_REQUESTS_QUERY, userRowMapper, userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, userRowMapper, userId, otherId);
    }

    public boolean friendshipExists(long userId, long friendId) {
        Integer count = jdbc.queryForObject(
                CHECK_FRIENDSHIP_QUERY,
                Integer.class,
                userId,
                friendId
        );
        return count != null && count > 0;
    }
}