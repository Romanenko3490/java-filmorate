package ru.yandex.practicum.filmorate.dal.mappers;


import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedEventRowMapper implements RowMapper<FeedEvent> {
    @Override
    public FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FeedEvent(
                rs.getLong("event_id"),
                rs.getLong("user_id"),
                FeedEvent.EventType.valueOf(rs.getString("event_type")),
                FeedEvent.Operation.valueOf(rs.getString("operation")),
                rs.getLong("entity_id"),
                rs.getTimestamp("timestamp").toInstant()
        );
    }
}
