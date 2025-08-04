package ru.yandex.practicum.filmorate.dal;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FeedRepositoryImpl extends BaseRepository<FeedEvent> implements FeedRepository {
    private static final String GET_FEED_QUERY =
            "SELECT event_id, user_id, event_type, operation, entity_id, timestamp " +
                    "FROM feed WHERE user_id = ? ORDER BY timestamp ASC";

    public FeedRepositoryImpl(JdbcTemplate jdbc, RowMapper<FeedEvent> mapper, Checker checker) {
        super(jdbc, mapper, checker);
    }

    @Override
    public List<FeedEvent> getFeedByUserId(Long userId) {
        checker.userExist(userId);
        return jdbc.query(GET_FEED_QUERY, mapper, userId);
    }

    @Override
    public FeedEvent addEvent(FeedEvent event) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbc)
                .withTableName("feed")
                .usingGeneratedKeyColumns("event_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("user_id", event.getUserId());
        parameters.put("event_type", event.getEventType().name());
        parameters.put("operation", event.getOperation().name());
        parameters.put("entity_id", event.getEntityId());
        parameters.put("timestamp", Timestamp.from(event.getTimestamp()));

        long eventId = insert.executeAndReturnKey(parameters).longValue();
        event.setEventId(eventId);
        return event;
    }
}
