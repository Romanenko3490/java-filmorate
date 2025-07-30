package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserDbService userDbService;
    private final FilmDbService filmDbService;


    //crud ops
    public ReviewDto createReview(NewReviewRequest request) {
        checkUserExists(request.getUserId());
        checkFilmExists(request.getFilmId());

        Review review = ReviewMapper.mapToReview(request);
        review = reviewRepository.save(review);
        log.info("Review created with id: {}", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto updateReview(UpdateReviewRequest request) {
        Review review = getReviewById(request.getReviewId());
        ReviewMapper.updateReview(review, request);
        reviewRepository.update(review);
        log.info("Review updated with id: {}", review.getReviewId());
        return ReviewMapper.mapToReviewDto(review);
    }

    public void deleteReview(Long reviewId) {
        if (!reviewRepository.delete(reviewId)) {
            throw new NotFoundException("Review with id: " + reviewId + " not found");
        }
        log.info("Review deleted with id: {}", reviewId);
    }

    public ReviewDto getById(Long reviewId) {
        Review review = getReviewById(reviewId);
        return ReviewMapper.mapToReviewDto(review);
    }

    public List<ReviewDto> getAllReviews(Long filmId, int count) {
        if (count <= 0) {
            throw new ValidationException("count must be greater than 0");
        }

        if (filmId != null) {
            checkFilmExists(filmId);
        }

        List<Review> reviews = filmId != null
                ? reviewRepository.findAllByFilmId(filmId, count)
                : reviewRepository.findAll(count);

        return reviews.stream().map(ReviewMapper::mapToReviewDto).collect(Collectors.toList());
    }
    //end


    //Likes ops
    public void addLike(long reviewId, long userId) {
        checkUserExists(userId);
        getReviewById(reviewId);

        reviewRepository.addLike(reviewId, userId);
        log.info("User {} added like to review : {}", userId, reviewId);
    }

    public void removeLike(long reviewId, long userId) {
        checkUserExists(userId);
        getReviewById(reviewId);

        reviewRepository.removeLike(reviewId, userId);
        log.info("User {} removed like to review : {}", userId, reviewId);
    }

    public void addDislike(long reviewId, long userId) {
        checkUserExists(userId);
        getReviewById(reviewId);

        reviewRepository.addDislike(reviewId, userId);
        log.info("User {} added dislike to review : {}", userId, reviewId);
    }

    public void removeDislike(long reviewId, long userId) {
        checkUserExists(userId);
        getReviewById(reviewId);

        reviewRepository.removeDislike(reviewId, userId);
        log.info("User {} removed dislike to review : {}", userId, reviewId);
    }
    //end


    //special methods
    private Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with id: " + id));
    }


    private void checkUserExists(long userId) {
        if (!userDbService.userExists(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
    }

    private void checkFilmExists(long filmId) {
        if (!filmDbService.filmExists(filmId)) {
            throw new NotFoundException("Film not found with id: " + filmId);
        }
    }
    //end
}
