package ru.yandex.practicum.filmorate.exception.errorhandling;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public class Violation {

    private final String fieldName;
    private final String message;

}
