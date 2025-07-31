package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FeedEventMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final UserDbService userDbService;
    private final FeedEventMapper mapper;


    public List<FeedEvent> getFeedByUserId(Long userId) {
        if (!userDbService.userExists(userId)) {
            log.info("User does not exist with id {}", userId);
            throw new NotFoundException("User does not exist with id " + userId);
        }
        return feedRepository.getFeedByUserId(userId);
    }


    private void addEvent(FeedEventDto eventDto) {
        feedRepository.addEvent(mapper.toFeedEvent(eventDto));
    }

    // Friends
    public void addFriendEvent(long userId, long friendId) {
        addEvent(new FeedEventDto(userId, FeedEvent.EventType.FRIEND, FeedEvent.Operation.ADD, friendId));
    }

    public void removeFriendEvent(long userId, long friendId) {
        addEvent(new FeedEventDto(userId, FeedEvent.EventType.FRIEND, FeedEvent.Operation.REMOVE, friendId));
    }

    // Film Likes
    public void addLikeEvent(long userId, long filmId) {
        addEvent(new FeedEventDto(userId, FeedEvent.EventType.LIKE, FeedEvent.Operation.ADD, filmId));
    }

    public void removeLikeEvent(long userId, long filmId) {
        addEvent(new FeedEventDto(userId, FeedEvent.EventType.LIKE, FeedEvent.Operation.REMOVE, filmId));
    }

    // Review Likes
    public void addReviewEvent(long userId, long reviewId) {
        addEvent(new FeedEventDto(userId, FeedEvent.EventType.REVIEW, FeedEvent.Operation.ADD, reviewId));
    }

    public void removeReviewEvent(long userId, long reviewId) {
        addEvent(new FeedEventDto(userId, FeedEvent.EventType.REVIEW, FeedEvent.Operation.REMOVE, reviewId));
    }


}
