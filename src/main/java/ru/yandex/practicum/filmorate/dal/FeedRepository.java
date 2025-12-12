package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

public interface FeedRepository {
    List<FeedEvent> getFeedByUserId(Long userId);

    FeedEvent addEvent(FeedEvent event);
}
