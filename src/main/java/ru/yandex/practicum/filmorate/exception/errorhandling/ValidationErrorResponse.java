package ru.yandex.practicum.filmorate.exception.errorhandling;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

// Resource https://struchkov.dev/blog/ru/spring-boot-validation/
@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {
    private final List<Violation> violations;
}
