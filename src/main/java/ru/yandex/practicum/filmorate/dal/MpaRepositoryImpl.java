package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class MpaRepositoryImpl extends BaseRepository<MpaRating> implements MpaRepository {
    private static final String EXISTS_BY_ID_QUERY =
            "SELECT EXISTS(SELECT 1 FROM mpa_rating WHERE mpa_id = ?)";
    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM mpa_rating WHERE mpa_id = ?";
    private static final String FIND_ALL_QUERY =
            "SELECT * FROM mpa_rating ORDER BY mpa_id";

    public MpaRepositoryImpl(JdbcTemplate jdbc, Checker checker) {
        super(jdbc, (rs, rowNum) -> {
            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            mpa.setDescription(rs.getString("description"));
            return mpa;
        },  checker);
    }

    @Override
    public boolean existsById(int mpaId) {
        return super.existsById(EXISTS_BY_ID_QUERY, mpaId);
    }

    @Override
    public Optional<MpaRating> findById(int mpaId) {
        log.debug("Getting MPA rating by id: {}", mpaId);
        return super.findById(FIND_BY_ID_QUERY, mpaId);
    }

    @Override
    public List<MpaRating> findAll() {
        log.debug("Getting all MPA ratings");
        return super.findAll(FIND_ALL_QUERY);
    }
}