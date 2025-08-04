package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final EntityCheckService entityCheckService;


    //crud ops
    public ReviewDto createReview(NewReviewRequest request) {
        entityCheckService.checkFilmExists(request.getFilmId());
        entityCheckService.checkUserExists(request.getUserId());

        Review review = ReviewMapper.mapToReview(request);
        review = reviewRepository.save(review);
        log.info("Review created with id: {}", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto updateReview(UpdateReviewRequest request) {
        entityCheckService.checkReviewExists(request.getReviewId());
        Review review = getReviewById(request.getReviewId());
        ReviewMapper.updateReview(review, request);
        reviewRepository.update(review);
        log.info("Review updated with id: {}", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public void deleteReview(Long reviewId) {
        entityCheckService.checkReviewExists(reviewId);
        if (!reviewRepository.delete(reviewId)) {
            throw new NotFoundException("Review with id: " + reviewId + " not found");
        }
        log.info("Review deleted with id: {}", reviewId);
    }

    public ReviewDto getById(Long reviewId) {
        entityCheckService.checkReviewExists(reviewId);
        Review review = getReviewById(reviewId);
        return ReviewMapper.mapToReviewDto(review);
    }

    public List<ReviewDto> getAllReviews(Long filmId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than 0");
        }

        if (filmId != null) {
            entityCheckService.checkFilmExists(filmId);
        }

        List<Review> reviews = filmId != null
                ? reviewRepository.findAllByFilmId(filmId, count)
                : reviewRepository.findAll(count);

        return reviews.stream().map(ReviewMapper::mapToReviewDto).collect(Collectors.toList());
    }

    public long getReviewAuthorId(long reviewId) {
        return reviewRepository.findAuthorIdByReviewId(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + reviewId));
    }
    //end


    //Likes ops
    public void addLike(long reviewId, long userId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkReviewExists(reviewId);

        reviewRepository.addLike(reviewId, userId);
        log.info("User {} added like to review : {}", userId, reviewId);
    }

    public void removeLike(long reviewId, long userId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkReviewExists(reviewId);

        reviewRepository.removeLike(reviewId, userId);
        log.info("User {} removed like to review : {}", userId, reviewId);
    }

    public void addDislike(long reviewId, long userId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkReviewExists(reviewId);

        reviewRepository.addDislike(reviewId, userId);
        log.info("User {} added dislike to review : {}", userId, reviewId);
    }

    public void removeDislike(long reviewId, long userId) {
        entityCheckService.checkUserExists(userId);
        entityCheckService.checkReviewExists(reviewId);

        reviewRepository.removeDislike(reviewId, userId);
        log.info("User {} removed dislike to review : {}", userId, reviewId);
    }
    //end


    //special methods
    private Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
    }

    //end
}
