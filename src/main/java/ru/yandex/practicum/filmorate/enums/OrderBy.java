package ru.yandex.practicum.filmorate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum OrderBy {
    YEAR("year", "release_date"),
    LIKES("likes", "likes_count");

    private final String param;
    private final String column;

    public static OrderBy fromParam(String param) {
        return Arrays.stream(OrderBy.values())
                .filter(orderBy -> orderBy.getParam().equals(param))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Invalid order by parameter: " + param));
    }
}
