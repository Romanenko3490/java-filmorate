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
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FeedEventMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.film.MpaRating;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
    private EntityCheckService entityCheckService;

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

    @BeforeEach
    void setUp() {
        // Очищаем ленту перед каждым тестом
        jdbcTemplate.update("DELETE FROM feed");
        log.info("Cleared feed table");

        // Проверяем существование тестовых пользователей
        assertUserExists(EXISTING_USER_ID_1);
        assertUserExists(EXISTING_USER_ID_2);

        // Создаем тестовый фильм
        testFilmId = createTestFilm();
        log.info("Created test film with id: {}", testFilmId);
    }

    private void assertUserExists(long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE user_id = ?",
                Integer.class,
                userId
        );
        assertThat(count).isEqualTo(1);
    }

    private long createTestFilm() {
        NewFilmRequest filmRequest = new NewFilmRequest();
        filmRequest.setName("Test Film");
        filmRequest.setDescription("Test Description");
        filmRequest.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmRequest.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(1L);
        filmRequest.setMpa(mpa);
        return filmDbService.addFilm(filmRequest).getId();
    }

    private void printFeedTableContent() {
        List<Map<String, Object>> feedContent = jdbcTemplate.queryForList("SELECT * FROM feed");
        log.info("Current feed table content ({} rows):", feedContent.size());
        feedContent.forEach(row -> log.info("Feed entry: {}", row));
    }

    private void printFriendshipTableContent() {
        List<Map<String, Object>> content = jdbcTemplate.queryForList("SELECT * FROM friendship");
        log.info("Current friendship table content ({} rows):", content.size());
        content.forEach(row -> log.info("Friendship entry: {}", row));
    }

    @Test
    void getFeedByUserId_shouldReturnEmptyListForUserWithoutEvents() {
        printFeedTableContent();
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed).isEmpty();
    }

    @Test
    void addFriendEvent_shouldCreateFriendEvent() {
        // 1. Добавляем друга через сервис
        log.info("Adding friend {} to user {}", EXISTING_USER_ID_2, EXISTING_USER_ID_1);
        userDbService.addFriend(EXISTING_USER_ID_1, EXISTING_USER_ID_2);

        // 2. Вручную добавляем событие в ленту (как это делает контроллер)
        feedService.addFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);

        printFriendshipTableContent();
        printFeedTableContent();

        // 3. Проверяем в БД напрямую
        Integer dbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM feed WHERE user_id = ? AND event_type = 'FRIEND' AND operation = 'ADD'",
                Integer.class,
                EXISTING_USER_ID_1
        );
        log.info("Found {} FRIEND/ADD events in DB", dbCount);
        assertThat(dbCount).isEqualTo(1);

        // 4. Проверяем через сервис
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        log.info("Feed from service: {}", feed);

        assertThat(feed)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getEventType()).isEqualTo(FeedEvent.EventType.FRIEND);
                    assertThat(event.getOperation()).isEqualTo(FeedEvent.Operation.ADD);
                    assertThat(event.getEntityId()).isEqualTo(EXISTING_USER_ID_2);
                    assertThat(event.getUserId()).isEqualTo(EXISTING_USER_ID_1);
                });
    }

    @Test
    void removeFriendEvent_shouldCreateRemoveOperationEvent() {
        // 1. Добавляем друга
        userDbService.addFriend(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        feedService.addFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);

        // 2. Проверяем, что событие добавления записалось
        Integer addCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM feed WHERE user_id = ? AND operation = 'ADD'",
                Integer.class,
                EXISTING_USER_ID_1
        );
        assertThat(addCount).isEqualTo(1);

        // 3. Удаляем друга
        userDbService.removeFriend(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        feedService.removeFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        printFeedTableContent();

        // 4. Проверяем оба события в БД
        Integer totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM feed WHERE user_id = ?",
                Integer.class,
                EXISTING_USER_ID_1
        );
        assertThat(totalCount).isEqualTo(2);

        // 5. Проверяем через сервис
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(2)
                .satisfiesExactly(
                        addEvent -> {
                            assertThat(addEvent.getEventType()).isEqualTo(FeedEvent.EventType.FRIEND);
                            assertThat(addEvent.getOperation()).isEqualTo(FeedEvent.Operation.ADD);
                            assertThat(addEvent.getEntityId()).isEqualTo(EXISTING_USER_ID_2);
                        },
                        removeEvent -> {
                            assertThat(removeEvent.getEventType()).isEqualTo(FeedEvent.EventType.FRIEND);
                            assertThat(removeEvent.getOperation()).isEqualTo(FeedEvent.Operation.REMOVE);
                            assertThat(removeEvent.getEntityId()).isEqualTo(EXISTING_USER_ID_2);
                        }
                );
    }

    @Test
    void addLikeEvent_shouldCreateLikeEvent() {
        // 1. Добавляем лайк
        filmDbService.addLike(testFilmId, EXISTING_USER_ID_1);
        feedService.addFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);
        printFeedTableContent();

        // 2. Проверяем в БД
        Integer dbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM feed WHERE user_id = ? AND event_type = 'LIKE'",
                Integer.class,
                EXISTING_USER_ID_1
        );
        assertThat(dbCount).isEqualTo(1);

        // 3. Проверяем через сервис
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
    void removeLikeEvent_shouldCreateRemoveOperation() throws InterruptedException {
        // 1. Добавляем лайк
        filmDbService.addLike(testFilmId, EXISTING_USER_ID_1);
        feedService.addFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);
        Thread.sleep(10);

        // 2. Удаляем лайк
        filmDbService.removeLike(testFilmId, EXISTING_USER_ID_1);
        feedService.removeFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);
        printFeedTableContent();

        // 3. Проверяем в БД
        Integer dbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM feed WHERE user_id = ? AND event_type = 'LIKE'",
                Integer.class,
                EXISTING_USER_ID_1
        );
        assertThat(dbCount).isEqualTo(2);

        // 4. Проверяем через сервис
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
    void addReviewEvent_shouldCreateReviewEvent() {
        // 1. Создаем отзыв
        NewReviewRequest request = new NewReviewRequest();
        request.setContent("Test review");
        request.setIsPositive(true);
        request.setUserId(EXISTING_USER_ID_1);
        request.setFilmId(testFilmId);

        ReviewDto review = reviewService.createReview(request);
        feedService.addReviewEvent(EXISTING_USER_ID_1, review.getReviewId());
        printFeedTableContent();

        // 2. Проверяем в БД
        Integer dbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM feed WHERE user_id = ? AND event_type = 'REVIEW'",
                Integer.class,
                EXISTING_USER_ID_1
        );
        assertThat(dbCount).isEqualTo(1);

        // 3. Проверяем через сервис
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(1)
                .first()
                .satisfies(event -> {
                    assertThat(event.getEventType()).isEqualTo(FeedEvent.EventType.REVIEW);
                    assertThat(event.getOperation()).isEqualTo(FeedEvent.Operation.ADD);
                    assertThat(event.getEntityId()).isEqualTo(review.getReviewId());
                });
    }

    @Test
    void events_shouldHaveCorrectTimestamps() {
        // 1. Добавляем события
        filmDbService.addLike(testFilmId, EXISTING_USER_ID_1);
        feedService.addFilmLikeEvent(EXISTING_USER_ID_1, testFilmId);

        userDbService.addFriend(EXISTING_USER_ID_1, EXISTING_USER_ID_2);
        feedService.addFriendEvent(EXISTING_USER_ID_1, EXISTING_USER_ID_2);

        printFeedTableContent();

        // 2. Проверяем порядок
        List<FeedEventDto> feed = feedService.getFeedByUserId(EXISTING_USER_ID_1);
        assertThat(feed)
                .hasSize(2)
                .isSortedAccordingTo(Comparator.comparing(FeedEventDto::getTimestamp));
    }

    @Test
    void getFeedByUserId_shouldThrowForNonExistentUser() {
        assertThrows(NotFoundException.class, () -> feedService.getFeedByUserId(999L));
    }
}