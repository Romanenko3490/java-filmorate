package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.Review;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {
    public static Review mapToReview(NewReviewRequest newReviewRequest) {
        Review review = new Review();
        review.setContent(newReviewRequest.getContent());
        review.setIsPositive(newReviewRequest.getIsPositive());
        review.setFilmId(newReviewRequest.getFilmId());
        review.setUserId(newReviewRequest.getUserId());
        review.setUseful(0);
        return review;
    }

    public static ReviewDto mapToReviewDto(Review review) {
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setReviewId(review.getReviewId());
        reviewDto.setContent(review.getContent());
        reviewDto.setIsPositive(review.getIsPositive());
        reviewDto.setFilmId(review.getFilmId());
        reviewDto.setUserId(review.getUserId());
        reviewDto.setUseful(review.getUseful());
        return reviewDto;
    }

    public static Review updateReview(Review review, UpdateReviewRequest request) {
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }
        if (request.getIsPositive() != null) {
            review.setIsPositive(request.getIsPositive());
        }
        return review;
    }
}
