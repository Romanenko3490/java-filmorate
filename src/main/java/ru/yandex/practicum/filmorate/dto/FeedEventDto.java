package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.FeedEvent;

@Data
@AllArgsConstructor
public class FeedEventDto {
    private Long eventId;
    private Long userId;
    private FeedEvent.EventType eventType;
    private FeedEvent.Operation operation;
    private Long entityId;
    private long timestamp;
}