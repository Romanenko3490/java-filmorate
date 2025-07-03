package ru.yandex.practicum.filmorate.model.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @NotNull(message = "Friend ID cannot be null")
    private Integer friendId;

    @NotNull(message = "Status cannot be null")
    private FriendshipStatus status;

    @PastOrPresent(message = "Creation date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate = LocalDate.now();
}

enum FriendshipStatus {
    PENDING,
    CONFIRMED
}