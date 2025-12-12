package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.Checker;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Service
@RequiredArgsConstructor
public class EntityCheckService {
    private final Checker checker;

    public void checkUserExists(long userId) {
        if (!checker.userExist(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
    }

    public void checkFilmExists(long filmId) {
        if (!checker.filmExists(filmId)) {
            throw new NotFoundException("Film not found with id: " + filmId);
        }
    }

    public void checkDirectorExists(long directorId) {
        if (!checker.directorExists(directorId)) {
            throw new NotFoundException("Director not found with id: " + directorId);
        }
    }

    public void checkReviewExists(long reviewId) {
        if (!checker.reviewExists(reviewId)) {
            throw new NotFoundException("Review not found with id: " + reviewId);
        }
    }

}
