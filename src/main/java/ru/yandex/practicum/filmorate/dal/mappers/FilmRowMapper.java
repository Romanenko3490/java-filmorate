package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        if (hasColumn(rs, "mpa_code")) { // Проверяем наличие столбца
            String mpaCode = rs.getString("mpa_code");
            if (mpaCode != null) {
                MpaRating mpa = new MpaRating();
                mpa.setId(rs.getInt("mpa_id"));
                mpa.setCode(mpaCode);
                mpa.setDescription(rs.getString("mpa_description"));
                film.setMpa(mpa);
            }
        }
        return film;
    }

    // Вспомогательный метод для проверки наличия столбца
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
