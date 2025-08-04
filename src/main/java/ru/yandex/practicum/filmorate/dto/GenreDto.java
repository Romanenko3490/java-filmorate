package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// GenreDto.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreDto implements Comparable<GenreDto> {
    private Long id;
    private String name;

    @Override
    public int compareTo(GenreDto other) {
        return this.id.compareTo(other.getId());
    }


}
