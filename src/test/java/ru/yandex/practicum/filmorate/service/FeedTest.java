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
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.dto.FeedEventDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FeedEventMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.film.MpaRating;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@Import({
        FeedService.class,
        FeedRepositoryImpl.class,
        UserRepository.class,
        FilmRepository.class,
        ReviewRepository.class,
        FriendshipRepository.class,
        UserRowMapper.class,
        FilmRowMapper.class,
        ReviewRowMapper.class,
        FeedEventRowMapper.class,
        FeedEventMapper.class,
        UserDbService.class,
        FilmDbService.class,
        ReviewService.class,
        GenreRepositoryImpl.class,
        EntityChecker.class,
        EntityCheckService.class,
        DirectorRepositoryImpl.class,
        DirectorRowMapper.class,
        MpaRepositoryImpl.class
})
@Sql(scripts = {"/schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FeedTest {

    @Autowired
    private FeedService feedService;

    @Autowired
    private UserDbService userDbService;

    @Autowired
    private FilmDbService filmDbService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long EXISTING_USER_ID_1 = 1L;
    private static final long EXISTING_USER_ID_2 = 2L;
    private long testFilmId;
    private long testReviewId;

    @BeforeEach
    void setUp() {
        clearFeedTable();
        testFilmId = createTestFilm();
        testReviewId = createTestReview();
        log.info("Test setup complete - created film with id: {} and review with id: {}", testFilmId, testReviewId);
    }

    private void clearFeedTable() {
        jdbcTemplate.update("DELETE FROM feed");
        log.debug("Cleared feed table");
    }

    private long createTestFilm() {
        NewFilmRequest filmRequest = new NewFilmRequest();
        filmRequest.setName("Test Film");
        filmRequest.setDescription("Test Description");
        filmRequest.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmRequest.setDuration(120);

        Set<MpaRating> mpaRatings = Set.of(new MpaRating(1L, "G", "General Audiences"));
        filmRequest.setMpa(mpaRatings);
        filmRequest.setDirectors(Set.of());

        return filmDbService.addFilm(filmRequest).getId();
    }

    private long createTestReview() {
        NewReviewRequest request = new NewReviewRequest();
        request.setContent("Test review");
        request.setIsPositive(true);
        request.setUserId(EXISTING_USER_ID_1);
        request.setFilmId(testFilmId);

        return reviewService.createReview(request).getReviewId();
    }

    private void printFeedTableContent() {
        List<Map<String, Object>> feedContent = jdbcTemplate.queryForList("SELECT * FROM feed ORDER BY timestamp");
        log.info("Feed table content ({} rows):", feedContent.size());
        feedContent.forEach(row -> log.info("{} | {} | {} | {} | {}",
                row.get("event_id"),
                row.get("user_id"),
                row.get("event_type"),
                row.get("operation"),
                row.get("entity_id")));
    }

    @Test
    void getFeedByUserId_shouldReturnEmptyListForUserWithoutEvents() {
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed).isEmpty();
    }

    @Test
    void addFriendEvent_shouldCreateFriendAddEvent() {
        // When
        userDbService.addFriend(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        feedService.addFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);

        // Then
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getUserId()).isEqualTo(EXISTING_USER_ID_1);
                    assertThat(event.getEventType()).isEqualTo(FeedEvent.EventType.FRIEND);
                    assertThat(event.getOperation()).isEqualTo(FeedEvent.Operation.ADD);
                    assertThat(event.getEntityId()).isEqualTo(EXISTING_USER_ID_2);
                    assertThat(event.getTimestamp()).isPositive();
                });
    }

    @Test
    void removeFriendEvent_shouldCreateFriendRemoveEvent() {
        // Setup - add friend first
        userDbService.addFriend(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        feedService.addFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);

        // When
        userDbService.removeFriend(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        feedService.removeFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);

        // Then
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(2)
                .satisfiesExactly(
                        addEvent -> assertThat(addEvent.getOperation()).isEqualTo(FeedEvent.Operation.ADD),
                        removeEvent -> {
                            assertThat(removeEvent.getOperation()).isEqualTo(FeedEvent.Operation.REMOVE);
                            assertThat(removeEvent.getEntityId()).isEqualTo(EXISTING_USER_ID_2);
                        }
                );
    }

    @Test
    void addLikeEvent_shouldCreateLikeAddEvent() {
        // When
        filmDbService.addLike(testFilmId, EXISTING_USER_ID_1);
        feedService.addFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);

        // Then
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getEventType()).isEqualTo(FeedEvent.EventType.LIKE);
                    assertThat(event.getOperation()).isEqualTo(FeedEvent.Operation.ADD);
                    assertThat(event.getEntityId()).isEqualTo(testFilmId);
                });
    }

    @Test
    void removeLikeEvent_shouldCreateLikeRemoveEvent() {
        // Setup - add like first
        filmDbService.addLike(testFilmId, EXISTING_USER_ID_1);
        feedService.addFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);

        // When
        filmDbService.removeLike(testFilmId, EXISTING_USER_ID_1);
        feedService.removeFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);

        // Then
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(2)
                .satisfiesExactly(
                        addEvent -> assertThat(addEvent.getOperation()).isEqualTo(FeedEvent.Operation.ADD),
                        removeEvent -> assertThat(removeEvent.getOperation()).isEqualTo(FeedEvent.Operation.REMOVE)
                );
    }

    @Test
    void reviewEvents_shouldCreateCorrectEvents() {
        // When
        feedService.addReviewEvent(EXISTING_USER_ID_1, testReviewId);
        feedService.updateReviewEvent(testReviewId);
        feedService.removeReviewEvent(testReviewId);

        // Then
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(3)
                .satisfiesExactly(
                        addEvent -> {
                            assertThat(addEvent.getEventType()).isEqualTo(FeedEvent.EventType.REVIEW);
                            assertThat(addEvent.getOperation()).isEqualTo(FeedEvent.Operation.ADD);
                        },
                        updateEvent -> {
                            assertThat(updateEvent.getEventType()).isEqualTo(FeedEvent.EventType.REVIEW);
                            assertThat(updateEvent.getOperation()).isEqualTo(FeedEvent.Operation.UPDATE);
                        },
                        removeEvent -> {
                            assertThat(removeEvent.getEventType()).isEqualTo(FeedEvent.EventType.REVIEW);
                            assertThat(removeEvent.getOperation()).isEqualTo(FeedEvent.Operation.REMOVE);
                        }
                );
    }

    @Test
    void reviewLikeEvents_shouldCreateCorrectEvents() {
        // When
        feedService.addReviewLikeEvent(EXISTING_USER_ID_1, testReviewId);
        feedService.removeReviewLikeEvent(EXISTING_USER_ID_1, testReviewId);

        // Then
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(2)
                .satisfiesExactly(
                        addEvent -> {
                            assertThat(addEvent.getEventType()).isEqualTo(FeedEvent.EventType.LIKE);
                            assertThat(addEvent.getOperation()).isEqualTo(FeedEvent.Operation.ADD);
                        },
                        removeEvent -> {
                            assertThat(removeEvent.getEventType()).isEqualTo(FeedEvent.EventType.LIKE);
                            assertThat(removeEvent.getOperation()).isEqualTo(FeedEvent.Operation.REMOVE);
                        }
                );
    }

    @Test
    void events_shouldBeOrderedByTimestamp() {
        // When
        feedService.addFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        feedService.addFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);
        feedService.addReviewEvent(EXISTING_USER_ID_1, testReviewId);

        // Then
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(3)
                .isSortedAccordingTo(Comparator.comparing(FeedEventDto::getTimestamp));
    }

    @Test
    void getFeedByUserId_shouldThrowForNonExistentUser() {
        assertThrows(NotFoundException.class, () -> feedService.getFeedByUserId(999L));
    }

    @Test
    void multipleEventTypes_shouldBeHandledCorrectly() {
        // When
        feedService.addFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        feedService.addFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);
        feedService.addReviewEvent(EXISTING_USER_ID_1, testReviewId);

        // Then
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(3)
                .extracting(FeedEventDto::getEventType)
                .containsExactlyInAnyOrder(
                        FeedEvent.EventType.FRIEND,
                        FeedEvent.EventType.LIKE,
                        FeedEvent.EventType.REVIEW
                );
    }
}