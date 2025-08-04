package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EntityChecker implements Checker {
    private final JdbcTemplate jdbc;



    @Override
    public boolean userExist(long userId) {
        String query = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, userId);
        return count != null && count > 0;
    }

    @Override
    public boolean filmExists(long id) {
        String query = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean directorExists(long id) {
        String query = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean genreExists(long id) {
        String query = "SELECT COUNT(*) FROM genre WHERE genre_id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean mpaExists(long id) {
        String query = "SELECT COUNT(*) FROM mpa_rating WHERE mpa_id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean reviewExists(long id) {
        String query = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }
}
