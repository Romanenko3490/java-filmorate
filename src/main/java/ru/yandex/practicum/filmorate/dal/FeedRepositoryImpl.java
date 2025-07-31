package ru.yandex.practicum.filmorate.dal;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<FeedEvent> mapper;
    private static final String GET_FEED_QUERY =
            "SELECT * FROM feed WHERE user_id = ? ORDER BY timestamp DESC";
    private static final String ADD_EVENT_QUERY =
            "INSERT INTO feed (user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?)";


    @Override
    public List<FeedEvent> getFeedByUserId(Long userId) {
        return jdbc.query(GET_FEED_QUERY, mapper, userId);
    }

    @Override
    public void addEvent(FeedEvent event) {
        jdbc.update(ADD_EVENT_QUERY,
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId());
    }
}
