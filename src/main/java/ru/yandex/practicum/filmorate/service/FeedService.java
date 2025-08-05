package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.mapper.FeedEventMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final FeedEventMapper mapper;
    private final ReviewService reviewService;
    private final EntityCheckService entityCheckService;


    public List<FeedEventDto> getFeedByUserId(Long userId) {
        entityCheckService.checkUserExists(userId);
        return feedRepository.getFeedByUserId(userId).stream()
                .map(mapper::toDto)
                .toList();
    }


    private void addEvent(FeedEventDto eventDto) {
        log.debug("Adding new event {}", eventDto);
        feedRepository.addEvent(mapper.toFeedEvent(eventDto));
    }

    // Friends events
    public void addFriendEvent(long userId, long friendId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkUserExists(friendId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.FRIEND, FeedEvent.Operation.ADD, friendId, Instant.now().toEpochMilli()));
    }

    public void removeFriendEvent(long userId, long friendId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkUserExists(friendId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.FRIEND, FeedEvent.Operation.REMOVE, friendId, Instant.now().toEpochMilli()));
    }

    // Film likes events
    public void addFilmLikeEvent(long userId, long filmId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkFilmExists(filmId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.LIKE, FeedEvent.Operation.ADD, filmId, Instant.now().toEpochMilli()));
    }

    public void removeFilmLikeEvent(long userId, long filmId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkFilmExists(filmId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.LIKE, FeedEvent.Operation.REMOVE, filmId, Instant.now().toEpochMilli()));
    }

    // Review events (not review likes!)
    public void addReviewEvent(long userId, long reviewId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkReviewExists(reviewId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.REVIEW, FeedEvent.Operation.ADD, reviewId, Instant.now().toEpochMilli()));
    }

    public void updateReviewEvent(long reviewId) {
        entityCheckService.checkReviewExists(reviewId);
        Long userId = reviewService.getReviewAuthorId(reviewId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.REVIEW, FeedEvent.Operation.UPDATE, reviewId, Instant.now().toEpochMilli()));
    }

    public void removeReviewEvent(long reviewId) {
        entityCheckService.checkReviewExists(reviewId);
        Long userId = reviewService.getReviewAuthorId(reviewId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.REVIEW, FeedEvent.Operation.REMOVE, reviewId, Instant.now().toEpochMilli()));
    }

    // Review likes events (treated as LIKE type)
    public void addReviewLikeEvent(long userId, long reviewId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkReviewExists(reviewId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.LIKE, FeedEvent.Operation.ADD, reviewId, Instant.now().toEpochMilli()));
    }

    public void removeReviewLikeEvent(long userId, long reviewId) {
        entityCheckService.checkUserExists(userId);
        addEvent(new FeedEventDto(null, userId, FeedEvent.EventType.LIKE, FeedEvent.Operation.REMOVE, reviewId, Instant.now().toEpochMilli()));
    }


}
