package ru.yandex.practicum.filmorate.dal;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.util.*;

@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> findById(String query, Object... args) {
        try {
            T result = jdbc.queryForObject(query, mapper, args);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findAll(String query, Object... args) {
        return jdbc.query(query, mapper, args);
    }

    protected boolean delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }

    protected void update(String query, Object... args) {
        int rowsUpdated = jdbc.update(query, args);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Error update process");
        }
    }

    protected long insert(String query, Object... args) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);

        Number id = keyHolder.getKey();

        if (id != null) {
            return id.longValue();
        } else {
            throw new InternalServerException("Save processing error");
        }
    }


    protected boolean existsById(String query, Object... args) {
        return Boolean.TRUE.equals(jdbc.queryForObject(query, Boolean.class, args));
    }

    protected Set<Integer> findAllExistingIds(String baseQuery, Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = String.format(baseQuery, placeholders);
        return new HashSet<>(jdbc.queryForList(sql, ids.toArray(), Integer.class));
    }
}
