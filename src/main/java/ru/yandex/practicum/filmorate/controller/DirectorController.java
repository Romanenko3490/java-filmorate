package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api//directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public List<DirectorDto> getAllGenres() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public DirectorDto getGenreById(@PathVariable @Positive int id) {
        return directorService.findById(id);
    }

    @PostMapping
    public DirectorDto add(@RequestBody Director director) {
        return directorService.add(director);
    }

    @PutMapping
    public DirectorDto update(@RequestBody Director director) {
        return directorService.update(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive int id) {
        directorService.delete(id);
    }

}
