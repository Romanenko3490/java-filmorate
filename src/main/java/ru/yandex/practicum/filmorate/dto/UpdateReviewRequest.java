package ru.yandex.practicum.filmorate.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @NotNull(message = "Review ID cannot be null")
    private Long reviewId;
    private String content;
    private Boolean isPositive;


}
