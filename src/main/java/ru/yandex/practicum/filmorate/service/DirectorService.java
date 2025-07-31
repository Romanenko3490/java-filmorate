package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.film.Director;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;

    public DirectorDto findById(long id) {
        return DirectorMapper.mapToDirectorDto(validateAndReturn(id));
    }

    public List<DirectorDto> findAll() {
        return directorRepository.findAll().stream()
                .map(DirectorMapper::mapToDirectorDto)
                .collect(Collectors.toList());
    }

    public DirectorDto add(Director  director) {
        return DirectorMapper.mapToDirectorDto(directorRepository.add(director));
    }

    public DirectorDto update(Director director) {
        validateAndReturn(director.getId());
        return DirectorMapper.mapToDirectorDto(directorRepository.update(director));
    }

    public void delete(long id) {
        validateAndReturn(id);
        directorRepository.delete(id);
    }

    private Director validateAndReturn(long id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Genre not found with id: " + id));
    }

}
