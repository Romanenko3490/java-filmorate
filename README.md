# ER Diagram для Filmorate Database

## Описание структуры базы данных

### Основные сущности

#### 1. Пользователи (`users`)

| Поле       | Тип       | Ограничения               | Описание                |
|------------|-----------|---------------------------|-------------------------|
| user_id    | INTEGER   | PRIMARY KEY               | Уникальный идентификатор|
| email      | VARCHAR   | NOT NULL, UNIQUE          | Электронная почта       |
| login      | VARCHAR   | NOT NULL                  | Логин пользователя      |
| name       | VARCHAR   |                           | Отображаемое имя        |
| birthday   | DATE      |                           | Дата рождения           |

#### 2. Фильмы (`film`)

| Поле          | Тип       | Ограничения               | Описание                |
|---------------|-----------|---------------------------|-------------------------|
| film_id       | INTEGER   | PRIMARY KEY               | Уникальный идентификатор|
| name          | VARCHAR   | NOT NULL                  | Название фильма         |
| description   | VARCHAR   | MAX(200)                  | Описание фильма         |
| release_date  | DATE      | NOT NULL                  | Дата выхода             |
| duration      | INTEGER   | NOT NULL, >0              | Продолжительность (мин) |
| mpa_rating_id | INTEGER   | FOREIGN KEY               | Ссылка на возрастной рейтинг |

### Справочные таблицы

#### 3. Жанры (`genre`)

| Поле       | Тип      | Ограничения               | Описание         |
|------------|----------|---------------------------|------------------|
| genre_id   | INTEGER  | PRIMARY KEY               | ID жанра         |
| name       | VARCHAR  | NOT NULL, UNIQUE          | Название жанра   |

#### 4. Возрастные рейтинги (`mpa_rating`)

| Поле         | Тип      | Ограничения               | Описание               |
|--------------|----------|---------------------------|------------------------|
| mpa_id       | INTEGER  | PRIMARY KEY               | ID рейтинга            |
| code         | VARCHAR  | NOT NULL, UNIQUE          | Код рейтинга (G, PG-13)|
| description  | VARCHAR  |                           | Описание рейтинга      |

### Таблицы связей

#### 5. Дружба (`friendship`)

| Поле         | Тип       | Ограничения               | Описание                |
|--------------|-----------|---------------------------|-------------------------|
| user_id      | INTEGER   | PRIMARY KEY, FOREIGN KEY  | ID пользователя         |
| friend_id    | INTEGER   | PRIMARY KEY, FOREIGN KEY  | ID друга                |
| status       | ENUM      | 'PENDING','CONFIRMED'     | Статус дружбы           |
| created_date | DATE      | DEFAULT CURRENT_DATE      | Дата создания           |

#### 6. Связь фильмов и жанров (`film_genre`)

| Поле     | Тип      | Ограничения              | Описание         |
|----------|----------|--------------------------|------------------|
| film_id  | INTEGER  | PRIMARY KEY, FOREIGN KEY | ID фильма        |
| genre_id | INTEGER  | PRIMARY KEY, FOREIGN KEY | ID жанра         |

#### 7. Лайки фильмов (`film_likes`)
e
| Поле     | Тип      | Ограничения              | Описание         |
|----------|----------|--------------------------|------------------|
| film_id  | INTEGER  | PRIMARY KEY, FOREIGN KEY | ID фильма        |
| user_id  | INTEGER  | PRIMARY KEY, FOREIGN KEY | ID пользователя  |

## Схема связей (Mermaid)

```mermaid
erDiagram
    USER {
        integer id
        varchar email
        varchar login
        varchar name
        date birthday
    }
    
    FILM {
        integer id
        varchar name
        varchar description
        date release_date
        integer duration
        integer mpa_rating_id
    }
    
    GENRE {
        integer id
        varchar name
    }
    
    MPA_RATING {
        integer id
        varchar code
        varchar description
    }
    
    USER ||--o{ FRIENDSHIP : "имеет"
    USER ||--o{ FILM_LIKE : "ставит"
    FILM ||--o{ FILM_GENRE : "имеет"
    FILM ||--o{ FILM_LIKE : "получает"
    FILM }|--|| MPA_RATING : "рейтинг"
    FILM_GENRE }|--|| GENRE : "жанр"



Примеры заапросов:

Получение списка друзей:
SELECT u.id, u.name, f.status
FROM users u
JOIN friendships f ON u.id = f.friend_id
WHERE f.user_id = 1;


Получение всех жанров:
SELECT * FROM genres;

Поиск фильмов по жанру:

SELECT f.title 
FROM films f
JOIN film_genres fg ON f.id = fg.film_id
WHERE fg.genre_id = 3; (мультфильм)