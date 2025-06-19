package ru.yandex.practicum.filmorate.exception.errorhandling;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Getter
@RequiredArgsConstructor
public class ViolationErrorResponse {
    private final List<Violation> violations;
}
