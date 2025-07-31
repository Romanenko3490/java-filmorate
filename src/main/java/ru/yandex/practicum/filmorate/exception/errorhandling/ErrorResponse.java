package ru.yandex.practicum.filmorate.exception.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private final String error;
    private final String description;
}
