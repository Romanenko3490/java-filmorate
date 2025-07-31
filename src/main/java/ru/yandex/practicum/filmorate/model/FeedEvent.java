package ru.yandex.practicum.filmorate.model;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class FeedEvent {
    private Long eventId;
    private Long userId;
    private EventType eventType;
    private Operation operation;
    private Long entityId;
    private Instant timestamp;

    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum Operation {
        ADD, REMOVE, UPDATE
    }

}
