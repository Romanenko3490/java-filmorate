package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.EntityCheckService;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final FeedService feedService;
    private final EntityCheckService entityCheckService;


    @PostMapping
    public ReviewDto createReview(@RequestBody NewReviewRequest request) {
        ReviewDto reviewDto = reviewService.createReview(request);
        feedService.addReviewEvent(reviewDto.getUserId(), reviewDto.getReviewId());
        return reviewDto;
    }


    @PutMapping
    public ReviewDto updateReview(@RequestBody UpdateReviewRequest request) {
        feedService.updateReviewEvent(request.getReviewId());
        return reviewService.updateReview(request);
    }


    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable("reviewId") Long reviewId) {
        Long userId = reviewService.getReviewAuthorId(reviewId);
        feedService.removeReviewEvent(userId, reviewId);
        reviewService.deleteReview(reviewId);
    }

    @GetMapping("/{reviewId}")
    public ReviewDto getReview(@PathVariable("reviewId") Long reviewId) {
        return reviewService.getById(reviewId);
    }

    @GetMapping
    public List<ReviewDto> getAllReviews(@RequestParam(required = false) Long filmId,
                                         @RequestParam(defaultValue = "10") Integer count) {
        return reviewService.getAllReviews(filmId, count);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.addLike(reviewId, userId);
        feedService.addReviewLikeEvent(userId, reviewId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDisLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void deleteLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.removeLike(reviewId, userId);
        feedService.removeReviewLikeEvent(userId, reviewId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void deleteDisLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.removeDislike(reviewId, userId);
    }
}
