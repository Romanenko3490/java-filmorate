package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewRepository extends BaseRepository<Review> {
    // region SQL Queries
    private static final String GET_ALL_REVIEWS_QUERY = "SELECT * FROM reviews";
    private static final String GET_REVIEW_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";
    private static final String UPDATE_REVIEW_QUERY =
            "UPDATE reviews SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";
    private static final String DELETE_REVIEW_QUERY = "DELETE FROM reviews WHERE review_id = ?";

    private static final String INSERT_REVIEW_LIKE_QUERY =
            "MERGE INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
    private static final String INSERT_REVIEW_DISLIKE_QUERY =
            "MERGE INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
    private static final String DELETE_REVIEW_LIKE_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
    private static final String DELETE_REVIEW_DISLIKE_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
    private static final String UPDATE_REVIEW_USEFUL_QUERY =
            "UPDATE reviews SET useful = " +
                    "(SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = true) - " +
                    "(SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND is_like = false) " +
                    "WHERE review_id = ?";

    private static final String GET_REVIEWS_BY_FILM_QUERY =
            "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String GET_ALL_REVIEWS_LIMITED_QUERY =
            "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
    private static final String GET_AUTHOR_ID_QUERY = "SELECT user_id FROM reviews WHERE review_id = ?";
    // endregion

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    // region CRUD Operations
    public List<Review> getAllReviews() {
        return jdbc.query(GET_ALL_REVIEWS_QUERY, mapper);
    }

    public Optional<Review> findById(long reviewId) {
        try {
            Review review = jdbc.queryForObject(GET_REVIEW_BY_ID_QUERY, mapper, reviewId);
            return Optional.ofNullable(review);
        } catch (Exception e) {
            log.warn("Review id {} not found", reviewId, e);
            return Optional.empty();
        }
    }

    public Review save(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbc)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("content", review.getContent());
        parameters.put("is_positive", review.getIsPositive());
        parameters.put("user_id", review.getUserId());
        parameters.put("film_id", review.getFilmId());
        parameters.put("useful", 0); // Всегда 0 при создании

        long reviewId = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        review.setReviewId(reviewId);
        return review;
    }

    public Review update(Review review) {
        int updated = jdbc.update(UPDATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
                review.getReviewId());

        if (updated == 0) {
            throw new NotFoundException("Review with id " + review.getReviewId() + " not found");
        }
        return review;
    }

    public boolean delete(long reviewId) {
        int deleted = jdbc.update(DELETE_REVIEW_QUERY, reviewId);
        return deleted > 0;
    }
    // endregion

    // region Like/Dislike Operations
    public void addLike(long reviewId, long userId) {
        jdbc.update(INSERT_REVIEW_LIKE_QUERY, reviewId, userId);
        updateUseful(reviewId);
    }

    public void addDislike(long reviewId, long userId) {
        jdbc.update(INSERT_REVIEW_DISLIKE_QUERY, reviewId, userId);
        updateUseful(reviewId);
    }

    public void removeLike(long reviewId, long userId) {
        jdbc.update(DELETE_REVIEW_LIKE_QUERY, reviewId, userId);
        updateUseful(reviewId);
    }

    public void removeDislike(long reviewId, long userId) {
        jdbc.update(DELETE_REVIEW_DISLIKE_QUERY, reviewId, userId);
        updateUseful(reviewId);
    }

    private void updateUseful(long reviewId) {
        jdbc.update(UPDATE_REVIEW_USEFUL_QUERY, reviewId, reviewId, reviewId);
    }
    // endregion

    // region Special Operations
    public List<Review> findAllByFilmId(Long filmId, int count) {
        return jdbc.query(GET_REVIEWS_BY_FILM_QUERY, mapper, filmId, count);
    }

    public List<Review> findAll(int count) {
        return jdbc.query(GET_ALL_REVIEWS_LIMITED_QUERY, mapper, count);
    }
    // endregion

    //assist
    public Optional<Long> findAuthorIdByReviewId(long reviewId) {
        try {
            return Optional.ofNullable(
                    jdbc.queryForObject(GET_AUTHOR_ID_QUERY, Long.class, reviewId)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}