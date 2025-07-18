package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaRepositoryImpl implements MpaRepository {
    private final JdbcTemplate jdbc;

    private final RowMapper<MpaRating> mpaRowMapper = (rs, rowNum) -> {
        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        mpa.setDescription(rs.getString("description"));
        return mpa;
    };

    @Override
    public boolean existsById(int mpaId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM mpa_rating WHERE mpa_id = ?)";
        return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, mpaId));
    }

    @Override
    public Optional<MpaRating> findById(int mpaId) {
        log.debug("Getting MPA rating by id: {}", mpaId);
        String sql = "SELECT * FROM mpa_rating WHERE mpa_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, mpaRowMapper, mpaId));
        } catch (EmptyResultDataAccessException e) {
            log.warn("MPA rating not found with id: {}", mpaId);
            return Optional.empty();
        }
    }

    @Override
    public List<MpaRating> findAll() {
        log.debug("Getting all MPA ratings");
        String sql = "SELECT * FROM mpa_rating ORDER BY mpa_id";
        return jdbc.query(sql, mpaRowMapper);
    }
}

