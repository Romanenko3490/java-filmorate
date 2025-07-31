package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.time.Instant;

@Component
public class FeedEventMapper {
    public FeedEvent toFeedEvent(FeedEventDto dto) {
        return new FeedEvent(
                null,
                dto.getUserId(),
                dto.getEventType(),
                dto.getOperation(),
                dto.getEntityId(),
                Instant.now()
        );
    }
}
