package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.film.Director;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class DirectorRepositoryImpl extends BaseRepository<Director> implements DirectorRepository {


    public DirectorRepositoryImpl(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors ORDER BY id ASC";
    private static final String ADD_DIRECTOR_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_DIRECTOR_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM directors WHERE id = ?";
    private static final String EXIST_BY_IDS_QUERY = "SELECT id FROM directors WHERE id IN (%s)";

    @Override
    public Optional<Director> findById(long directorId) {
        return findById(FIND_BY_ID_QUERY, directorId);
    }

    @Override
    public List<Director> findAll() {
        return findAll(FIND_ALL_QUERY);
    }

    @Override
    public Director add(Director director) {
        long id = insert(ADD_DIRECTOR_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        update(UPDATE_DIRECTOR_QUERY, director.getName(), director.getId());
        return director;
    }

    @Override
    public void delete(long id) {
        delete(DELETE_DIRECTOR_QUERY, id);
    }

    @Override
    public boolean existAllByIds(Set<Long> ids) {
        return ids.size() == findAllExistingIds(EXIST_BY_IDS_QUERY, ids).size();
    }

}
