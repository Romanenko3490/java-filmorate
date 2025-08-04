package ru.yandex.practicum.filmorate.dal;

public interface Checker {
    boolean userExist(long userId);
    boolean filmExists(long id);
    boolean directorExists(long id);
    boolean genreExists(long id);
    boolean mpaExists(long id);
    boolean reviewExists(long id);
}