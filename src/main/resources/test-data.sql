
MERGE INTO mpa_rating (mpa_id, mpa_name, description)
VALUES
    (1, 'G', 'Нет возрастных ограничений'),
    (2, 'PG', 'Рекомендуется присутствие родителей'),
    (3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
    (4, 'R', 'Лицам до 17 лет обязательно присутствие взрослого'),
    (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');


MERGE  INTO genre (genre_id, name)
VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');


MERGE  INTO users (user_id, email, login, name, birthday)
VALUES
    (1, 'user1@example.com', 'login1', 'User One', '1990-01-01'),
    (2, 'user2@example.com', 'login2', 'User Two', '1995-01-01');