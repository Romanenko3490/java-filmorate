package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

@Data
public class MpaDto implements Comparable<MpaDto> {
    private Long id;
    private String name;

    public MpaDto() {
    }

    public MpaDto(Long id) {
        this.id = id;
    }

    public MpaDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(MpaDto o) {
        return this.id.compareTo(o.getId());
    }
}
