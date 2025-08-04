package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Slf4j
@Import({
        ReviewService.class,
        ReviewRepository.class,
        UserRepository.class,
        UserDbService.class,
        FilmDbService.class,
        MpaRepositoryImpl.class,
        FilmRepository.class,
        FriendshipRepository.class,
        GenreRepositoryImpl.class,
        DirectorRepositoryImpl.class,
        EntityChecker.class,
        EntityCheckService.class,
        ReviewRowMapper.class,
        UserRowMapper.class,
        FilmRowMapper.class,
        DirectorRowMapper.class
})
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReviewTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private NewReviewRequest newReviewRequest;
    private UpdateReviewRequest updateReviewRequest;
    private NewFilmRequest newFilmRequest;
    private NewUserRequest newUserRequest;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO films (film_id, name, description, release_date, duration, mpa_rating_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                1L, "Test Film", "Description", LocalDate.of(2000, 1, 1), 120, 1  // Используем существующий mpa_id=1
        );

        jdbcTemplate.update(
                "INSERT INTO films (film_id, name, description, release_date, duration, mpa_rating_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                2L, "Another Film", "Another Description", LocalDate.of(2001, 1, 1), 90, 2  // Используем существующий mpa_id=2
        );

        newReviewRequest = new NewReviewRequest();
        newReviewRequest.setContent("Great film!");
        newReviewRequest.setIsPositive(true);
        newReviewRequest.setUserId(1L);
        newReviewRequest.setFilmId(1L);

        updateReviewRequest = new UpdateReviewRequest();
        updateReviewRequest.setReviewId(1L);
        updateReviewRequest.setContent("Updated review");
        updateReviewRequest.setIsPositive(false);
    }

    @Test
    void shouldCreateAndGetReview() {
        ReviewDto createdReview = reviewService.createReview(newReviewRequest);
        ReviewDto retrievedReview = reviewService.getById(createdReview.getReviewId());

        assertThat(retrievedReview.getReviewId()).isEqualTo(createdReview.getReviewId());
        assertThat(retrievedReview.getContent()).isEqualTo("Great film!");
        assertThat(retrievedReview.getIsPositive()).isTrue();
        assertThat(retrievedReview.getUseful()).isZero();
    }

    @Test
    void shouldUpdateReview() {
        ReviewDto createdReview = reviewService.createReview(newReviewRequest);
        System.out.println("review " + createdReview);

        UpdateReviewRequest updateRequest = new UpdateReviewRequest();
        updateRequest.setReviewId(createdReview.getReviewId());
        updateRequest.setContent("Updated review");
        updateRequest.setIsPositive(false);
        System.out.println("request " + updateRequest);

        ReviewDto updatedReview = reviewService.updateReview(updateRequest);
        System.out.println("updated " + updatedReview);
        assertThat(updatedReview.getContent()).isEqualTo("Updated review");
        assertThat(updatedReview.getIsPositive()).isFalse();
    }

    @Test
    void shouldDeleteReview() {
        ReviewDto createdReview = reviewService.createReview(newReviewRequest);
        reviewService.deleteReview(createdReview.getReviewId());

        assertThrows(NotFoundException.class,
                () -> reviewService.getById(createdReview.getReviewId()));
    }

    @Test
    void shouldGetAllReviews() {
        reviewService.createReview(newReviewRequest);

        NewReviewRequest anotherReview = new NewReviewRequest();
        anotherReview.setContent("Another review");
        anotherReview.setIsPositive(false);
        anotherReview.setUserId(2L);
        anotherReview.setFilmId(2L);
        reviewService.createReview(anotherReview);

        List<ReviewDto> reviews = reviewService.getAllReviews(null, 10);
        assertThat(reviews).hasSize(2);
    }

    @Test
    void shouldGetReviewsByFilmId() {
        reviewService.createReview(newReviewRequest);

        NewReviewRequest sameFilmReview = new NewReviewRequest();
        sameFilmReview.setContent("Second review");
        sameFilmReview.setIsPositive(true);
        sameFilmReview.setUserId(2L);
        sameFilmReview.setFilmId(1L); // Тот же filmId
        reviewService.createReview(sameFilmReview);

        List<ReviewDto> reviews = reviewService.getAllReviews(1L, 10);
        assertThat(reviews).hasSize(2);
        assertThat(reviews.get(0).getFilmId()).isEqualTo(1L);
        assertThat(reviews.get(1).getFilmId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenCountIsNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> reviewService.getAllReviews(null, -1));
    }

    @Test
    @Transactional
    void shouldAddAndRemoveLike() {
        ReviewDto review = reviewService.createReview(newReviewRequest);
        reviewService.addLike(review.getReviewId(), 2L);

        assertThat(getReviewUseful(review.getReviewId()))
                .as("Rating shell increase after like")
                .isEqualTo(1);

        reviewService.removeLike(review.getReviewId(), 2L);
        assertThat(getReviewUseful(review.getReviewId()))
                .as("Rating shell decrease after like")
                .isZero();
    }

    @Test
    @Transactional
    void shouldAddAndRemoveDislike() {
        ReviewDto review = reviewService.createReview(newReviewRequest);
        reviewService.addDislike(review.getReviewId(), 2L);

        assertThat(getReviewUseful(review.getReviewId()))
                .as("Rating shell decrease after dislike")
                .isEqualTo(-1);

        reviewService.removeDislike(review.getReviewId(), 2L);
        assertThat(getReviewUseful(review.getReviewId()))
                .as("Rating shell become 0 after dislike delete")
                .isZero();
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        NewReviewRequest invalidRequest = new NewReviewRequest();
        invalidRequest.setContent("Test");
        invalidRequest.setIsPositive(true);
        invalidRequest.setUserId(999L);
        invalidRequest.setFilmId(1L);

        assertThrows(NotFoundException.class,
                () -> reviewService.createReview(invalidRequest));
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        NewReviewRequest invalidRequest = new NewReviewRequest();
        invalidRequest.setContent("Test");
        invalidRequest.setIsPositive(true);
        invalidRequest.setUserId(1L);
        invalidRequest.setFilmId(999L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reviewService.createReview(invalidRequest));

        assertThat(exception.getMessage()).contains("Film not found with id: 999");
    }

    @Test
    void shouldSortByUseful() {
        ReviewDto review1 = reviewService.createReview(newReviewRequest);
        ReviewDto review2 = createAnotherReview();

        reviewService.addLike(review1.getReviewId(), 2L);
        reviewService.addLike(review1.getReviewId(), 1L);
        reviewService.addDislike(review2.getReviewId(), 2L);

        List<ReviewDto> reviews = reviewService.getAllReviews(null, 10);

        assertThat(reviews.get(0).getReviewId()).isEqualTo(review1.getReviewId());
        assertThat(reviews.get(0).getUseful()).isEqualTo(2);
        assertThat(reviews.get(1).getReviewId()).isEqualTo(review2.getReviewId());
        assertThat(reviews.get(1).getUseful()).isEqualTo(-1);
    }

    private ReviewDto createAnotherReview() {
        NewReviewRequest request = new NewReviewRequest();
        request.setContent("Another review");
        request.setIsPositive(false);
        request.setUserId(2L);
        request.setFilmId(2L);
        return reviewService.createReview(request);
    }

    private int getReviewUseful(long reviewId) {
        Integer useful = jdbcTemplate.queryForObject(
                "SELECT useful FROM reviews WHERE review_id = ?",
                Integer.class,
                reviewId
        );
        return useful != null ? useful : 0;
    }
}