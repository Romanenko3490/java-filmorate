package ru.yandex.practicum.filmorate.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;


    //Добавление нового отзыва
    @PostMapping
    public ReviewDto createReview(@RequestBody NewReviewRequest request) {
        return reviewService.createReview(request);
    }


    //Редактирование уже имеющегося отзыва.
    @PutMapping
    public ReviewDto updateReview(@RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(request);
    }


    //Удаление уже имеющегося отзыва
    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable("reviewId") Long reviewId) {
        reviewService.deleteReview(reviewId);
    }

    //Получение отзыва по идентификатору.
    @GetMapping("/{reviewId}")
    public ReviewDto getReview(@PathVariable("reviewId") Long reviewId) {
        return reviewService.getById(reviewId);
    }

    //Получение всех отзывов по идентификатору фильма, если фильм не указан то все. Если кол-во не указано то 10.
    @GetMapping
    public List<ReviewDto> getAllReviews(@RequestParam(required = false) Long filmId,
                                         @RequestParam(defaultValue = "10") Integer count) {
        return reviewService.getAllReviews(filmId, count);
    }

    //пользователь ставит лайк отзыву.
    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.addLike(reviewId, userId);
    }

    //пользователь ставит дизлайк отзыву.
    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDisLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.addDislike(reviewId, userId);
    }

    //пользователь удаляет лайк
    @DeleteMapping("/{reviewId}/like/{userId}")
    public void deleteLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.removeLike(reviewId, userId);
    }

    //пользователь удаляет дизлайк отзыву.
    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void deleteDisLike(@PathVariable Long reviewId, @PathVariable Long userId) {
        reviewService.removeDislike(reviewId, userId);
    }


}
